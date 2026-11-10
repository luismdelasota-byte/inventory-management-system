package com.inventory.modules.products.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDto {
    private String id;
    private String sku;
    private String name;
    private String description;
    private String categoryId;
    private String categoryName;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private Integer stock;
    private Integer minimumStock;
    private String status;
    private String createdAt;
}
