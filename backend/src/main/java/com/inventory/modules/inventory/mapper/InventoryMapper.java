package com.inventory.modules.inventory.mapper;

import com.inventory.modules.inventory.dto.InventoryItemDto;
import com.inventory.modules.products.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public InventoryItemDto toDto(Product product) {
        if (product == null) return null;

        String inventoryStatus;
        if (product.getStockQuantity() == null || product.getStockQuantity() <= 0) {
            inventoryStatus = "OUT_OF_STOCK";
        } else if (product.getStockQuantity() <= product.getMinimumStock()) {
            inventoryStatus = "LOW_STOCK";
        } else {
            inventoryStatus = "AVAILABLE";
        }

        return InventoryItemDto.builder()
                .id(String.valueOf(product.getId()))
                .sku(product.getSku())
                .name(product.getName())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .stock(product.getStockQuantity())
                .minimumStock(product.getMinimumStock())
                .inventoryStatus(inventoryStatus)
                .build();
    }
}
