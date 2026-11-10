package com.inventory.modules.movements.service;

import com.inventory.common.exception.BadRequestException;
import com.inventory.common.exception.InsufficientStockException;
import com.inventory.common.exception.ResourceNotFoundException;
import com.inventory.common.response.PagedResponse;
import com.inventory.modules.movements.dto.MovementItemRequestDto;
import com.inventory.modules.movements.dto.MovementRequestDto;
import com.inventory.modules.movements.dto.MovementResponseDto;
import com.inventory.modules.movements.entity.MovementType;
import com.inventory.modules.movements.entity.StockMovement;
import com.inventory.modules.movements.entity.StockMovementItem;
import com.inventory.modules.movements.mapper.MovementMapper;
import com.inventory.modules.movements.repository.StockMovementRepository;
import com.inventory.modules.products.entity.Product;
import com.inventory.modules.products.repository.ProductRepository;
import com.inventory.modules.users.entity.User;
import com.inventory.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovementServiceImpl implements MovementService {

    private final StockMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final MovementMapper movementMapper;

    @Override
    @Transactional
    public MovementResponseDto createMovement(MovementType type, MovementRequestDto request, String username) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Movement must include at least one item.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found: " + username));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BadRequestException("User account is inactive.");
        }

        // Validate duplicate products in same request
        Set<Long> productIds = new HashSet<>();
        for (MovementItemRequestDto itemDto : request.getItems()) {
            if (!productIds.add(itemDto.getProductId())) {
                throw new BadRequestException("Duplicate product ID found in movement request: " + itemDto.getProductId());
            }
            if (itemDto.getQuantity() == null || itemDto.getQuantity() <= 0) {
                throw new BadRequestException("Item quantity must be greater than zero.");
            }
        }

        // Generate movement code (MOV-0001 format)
        long currentCount = movementRepository.count();
        String code = String.format("MOV-%04d", currentCount + 1);

        StockMovement movement = StockMovement.builder()
                .code(code)
                .movementType(type)
                .reason(request.getReason().trim())
                .user(user)
                .build();

        for (MovementItemRequestDto itemDto : request.getItems()) {
            // Lock product for update to prevent concurrent race conditions
            Product product = productRepository.findByIdForUpdate(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDto.getProductId()));

            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new BadRequestException("Product is inactive and cannot be moved: " + product.getName());
            }

            int previousStock = product.getStockQuantity();
            int newStock;

            if (type == MovementType.IN) {
                newStock = previousStock + itemDto.getQuantity();
            } else if (type == MovementType.OUT) {
                if (previousStock < itemDto.getQuantity()) {
                    throw new InsufficientStockException("Insufficient stock for product '" + product.getName() +
                            "'. Available: " + previousStock + ", Requested: " + itemDto.getQuantity());
                }
                newStock = previousStock - itemDto.getQuantity();
            } else {
                throw new BadRequestException("Unsupported movement type: " + type);
            }

            product.setStockQuantity(newStock);
            productRepository.save(product);

            StockMovementItem item = StockMovementItem.builder()
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .previousStock(previousStock)
                    .newStock(newStock)
                    .build();

            movement.addItem(item);
        }

        StockMovement savedMovement = movementRepository.save(movement);
        return movementMapper.toResponseDto(savedMovement);
    }

    @Override
    @Transactional(readOnly = true)
    public MovementResponseDto getMovementById(Long id) {
        StockMovement movement = movementRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movement not found with id: " + id));
        return movementMapper.toResponseDto(movement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovementResponseDto> getAllMovementsList() {
        return movementRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(movementMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MovementResponseDto> getAllMovements(MovementType type, String search, Pageable pageable) {
        Page<StockMovement> page = movementRepository.findAllWithFilters(type, search, pageable);
        List<MovementResponseDto> dtos = page.getContent().stream()
                .map(movementMapper::toResponseDto)
                .collect(Collectors.toList());

        return PagedResponse.<MovementResponseDto>builder()
                .success(true)
                .message("Movements retrieved successfully")
                .data(dtos)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
