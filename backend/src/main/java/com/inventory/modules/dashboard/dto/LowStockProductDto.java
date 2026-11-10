package com.inventory.modules.dashboard.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LowStockProductDto {
    private String id;
    private String sku;
    private String name;
    private String categoryName;
    private Integer stock;
    private Integer minimumStock;
}
