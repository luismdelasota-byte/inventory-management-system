package com.inventory.modules.categories.service;

import com.inventory.common.exception.ConflictException;
import com.inventory.common.exception.ResourceNotFoundException;
import com.inventory.common.response.PagedResponse;
import com.inventory.modules.categories.dto.CategoryResponseDto;
import com.inventory.modules.categories.dto.CreateCategoryRequestDto;
import com.inventory.modules.categories.dto.UpdateCategoryRequestDto;
import com.inventory.modules.categories.entity.Category;
import com.inventory.modules.categories.mapper.CategoryMapper;
import com.inventory.modules.categories.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAllCategoriesList() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CategoryResponseDto> getAllCategories(String search, Pageable pageable) {
        Page<Category> categoryPage = categoryRepository.findAllWithSearch(search, pageable);
        List<CategoryResponseDto> dtos = categoryPage.getContent().stream()
                .map(categoryMapper::toResponseDto)
                .collect(Collectors.toList());

        return PagedResponse.<CategoryResponseDto>builder()
                .success(true)
                .message("Categories retrieved successfully")
                .data(dtos)
                .page(categoryPage.getNumber())
                .size(categoryPage.getSize())
                .totalElements(categoryPage.getTotalElements())
                .totalPages(categoryPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getActiveCategories() {
        return categoryRepository.findAllByActiveTrue().stream()
                .map(categoryMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return categoryMapper.toResponseDto(category);
    }

    @Override
    @Transactional
    public CategoryResponseDto createCategory(CreateCategoryRequestDto request) {
        String trimmedName = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new ConflictException("Category already exists with name: " + trimmedName);
        }

        Category category = Category.builder()
                .name(trimmedName)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .active(true)
                .build();

        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public CategoryResponseDto updateCategory(Long id, UpdateCategoryRequestDto request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        String trimmedName = request.getName().trim();
        if (!category.getName().equalsIgnoreCase(trimmedName) && categoryRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new ConflictException("Category already exists with name: " + trimmedName);
        }

        category.setName(trimmedName);
        category.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        Category updated = categoryRepository.save(category);
        return categoryMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public CategoryResponseDto toggleCategoryStatus(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setActive(!Boolean.TRUE.equals(category.getActive()));
        Category updated = categoryRepository.save(category);
        return categoryMapper.toResponseDto(updated);
    }
}
