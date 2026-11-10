package com.inventory.modules.movements.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovementLineDto {
    private String productId;
    private String productName;
    private Integer quantity;
    private Integer stockBefore;
    private Integer stockAfter;
}
