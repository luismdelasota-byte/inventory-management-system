package com.inventory.modules.categories.service;

import com.inventory.common.response.PagedResponse;
import com.inventory.modules.categories.dto.CategoryResponseDto;
import com.inventory.modules.categories.dto.CreateCategoryRequestDto;
import com.inventory.modules.categories.dto.UpdateCategoryRequestDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    List<CategoryResponseDto> getAllCategoriesList();
    PagedResponse<CategoryResponseDto> getAllCategories(String search, Pageable pageable);
    List<CategoryResponseDto> getActiveCategories();
    CategoryResponseDto getCategoryById(Long id);
    CategoryResponseDto createCategory(CreateCategoryRequestDto request);
    CategoryResponseDto updateCategory(Long id, UpdateCategoryRequestDto request);
    CategoryResponseDto toggleCategoryStatus(Long id);
}
