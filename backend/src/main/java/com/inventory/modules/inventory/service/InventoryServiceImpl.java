package com.inventory.modules.inventory.service;

import com.inventory.common.response.PagedResponse;
import com.inventory.modules.inventory.dto.InventoryItemDto;
import com.inventory.modules.inventory.mapper.InventoryMapper;
import com.inventory.modules.products.entity.Product;
import com.inventory.modules.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final InventoryMapper inventoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemDto> getInventoryList(String search) {
        return productRepository.findAllWithCategory(search).stream()
                .map(inventoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InventoryItemDto> getInventory(String search, Long categoryId, String status, Pageable pageable) {
        Page<Product> productsPage = productRepository.findAllWithFilters(search, categoryId, true, pageable);
        List<InventoryItemDto> allDtos = productsPage.getContent().stream()
                .map(inventoryMapper::toDto)
                .filter(item -> status == null || status.isBlank() || item.getInventoryStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());

        return PagedResponse.<InventoryItemDto>builder()
                .success(true)
                .message("Inventory retrieved successfully")
                .data(allDtos)
                .page(productsPage.getNumber())
                .size(productsPage.getSize())
                .totalElements(productsPage.getTotalElements())
                .totalPages(productsPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemDto> getLowStockItems() {
        return productRepository.findLowOrOutOfStockProducts().stream()
                .map(inventoryMapper::toDto)
                .collect(Collectors.toList());
    }
}
