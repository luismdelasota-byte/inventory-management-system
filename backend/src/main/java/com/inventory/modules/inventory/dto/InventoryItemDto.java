package com.inventory.modules.inventory.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemDto {
    private String id;
    private String sku;
    private String name;
    private String categoryName;
    private Integer stock;
    private Integer minimumStock;
    private String inventoryStatus;
}
