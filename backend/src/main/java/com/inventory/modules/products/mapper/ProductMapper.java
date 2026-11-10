package com.inventory.modules.products.mapper;

import com.inventory.modules.products.dto.ProductResponseDto;
import com.inventory.modules.products.entity.Product;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class ProductMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public ProductResponseDto toResponseDto(Product product) {
        if (product == null) return null;
        return ProductResponseDto.builder()
                .id(String.valueOf(product.getId()))
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .categoryId(product.getCategory() != null ? String.valueOf(product.getCategory().getId()) : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .purchasePrice(product.getPurchasePrice())
                .salePrice(product.getSalePrice())
                .stock(product.getStockQuantity())
                .minimumStock(product.getMinimumStock())
                .status(Boolean.TRUE.equals(product.getActive()) ? "ACTIVE" : "INACTIVE")
                .createdAt(product.getCreatedAt() != null ? product.getCreatedAt().format(DATE_FORMATTER) : null)
                .build();
    }
}
