package com.inventory.modules.categories.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponseDto {
    private String id;
    private String name;
    private String description;
    private String status;
}
