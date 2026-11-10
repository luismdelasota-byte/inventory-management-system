package com.inventory.modules.dashboard.service;

import com.inventory.modules.categories.repository.CategoryRepository;
import com.inventory.modules.dashboard.dto.DashboardSummaryDto;
import com.inventory.modules.dashboard.dto.LowStockProductDto;
import com.inventory.modules.movements.dto.MovementResponseDto;
import com.inventory.modules.movements.entity.MovementType;
import com.inventory.modules.movements.mapper.MovementMapper;
import com.inventory.modules.movements.repository.StockMovementRepository;
import com.inventory.modules.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockMovementRepository movementRepository;
    private final MovementMapper movementMapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryDto getSummary() {
        long totalProducts = productRepository.countByActiveTrue();
        long totalCategories = categoryRepository.countByActiveTrue();
        long lowStockProducts = productRepository.countLowStock();
        long outOfStockProducts = productRepository.countOutOfStock();
        long totalMovements = movementRepository.count();

        // Calculate entries and exits for current calendar month
        LocalDate now = LocalDate.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).atTime(LocalTime.MAX);

        long entriesThisMonth = movementRepository.countByMovementTypeAndCreatedAtBetween(MovementType.IN, startOfMonth, endOfMonth);
        long exitsThisMonth = movementRepository.countByMovementTypeAndCreatedAtBetween(MovementType.OUT, startOfMonth, endOfMonth);

        // Get 5 most recent movements
        List<MovementResponseDto> recentMovements = movementRepository.findRecentMovements(PageRequest.of(0, 5))
                .stream()
                .map(movementMapper::toResponseDto)
                .collect(Collectors.toList());

        // Get low stock products list
        List<LowStockProductDto> lowStockItems = productRepository.findLowStockProducts().stream()
                .map(p -> LowStockProductDto.builder()
                        .id(String.valueOf(p.getId()))
                        .sku(p.getSku())
                        .name(p.getName())
                        .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                        .stock(p.getStockQuantity())
                        .minimumStock(p.getMinimumStock())
                        .build())
                .collect(Collectors.toList());

        return DashboardSummaryDto.builder()
                .totalProducts(totalProducts)
                .totalCategories(totalCategories)
                .lowStockProducts(lowStockProducts)
                .outOfStockProducts(outOfStockProducts)
                .totalMovements(totalMovements)
                .entriesThisMonth(entriesThisMonth)
                .exitsThisMonth(exitsThisMonth)
                .recentMovements(recentMovements)
                .lowStockItems(lowStockItems)
                .build();
    }
}
