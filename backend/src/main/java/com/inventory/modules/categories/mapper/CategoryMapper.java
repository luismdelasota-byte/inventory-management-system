package com.inventory.modules.categories.mapper;

import com.inventory.modules.categories.dto.CategoryResponseDto;
import com.inventory.modules.categories.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponseDto toResponseDto(Category category) {
        if (category == null) return null;
        return CategoryResponseDto.builder()
                .id(String.valueOf(category.getId()))
                .name(category.getName())
                .description(category.getDescription())
                .status(Boolean.TRUE.equals(category.getActive()) ? "ACTIVE" : "INACTIVE")
                .build();
    }
}
