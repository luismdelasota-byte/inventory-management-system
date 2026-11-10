package com.inventory.modules.categories.service;

import com.inventory.common.exception.ConflictException;
import com.inventory.common.exception.ResourceNotFoundException;
import com.inventory.modules.categories.dto.CategoryResponseDto;
import com.inventory.modules.categories.dto.CreateCategoryRequestDto;
import com.inventory.modules.categories.dto.UpdateCategoryRequestDto;
import com.inventory.modules.categories.entity.Category;
import com.inventory.modules.categories.mapper.CategoryMapper;
import com.inventory.modules.categories.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Computers")
                .description("Computing devices")
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Create category with unique name succeeds")
    void createCategory_Success() {
        CreateCategoryRequestDto request = new CreateCategoryRequestDto("Computers", "Computing devices");

        when(categoryRepository.existsByNameIgnoreCase("Computers")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponseDto(category)).thenReturn(
                CategoryResponseDto.builder().id("1").name("Computers").status("ACTIVE").build()
        );

        CategoryResponseDto response = categoryService.createCategory(request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Computers");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Create duplicate category throws ConflictException")
    void createCategory_Duplicate_ThrowsConflict() {
        CreateCategoryRequestDto request = new CreateCategoryRequestDto("Computers", "Computing devices");
        when(categoryRepository.existsByNameIgnoreCase("Computers")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Category already exists");
    }

    @Test
    @DisplayName("Toggle category status successfully")
    void toggleCategoryStatus_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponseDto(category)).thenReturn(
                CategoryResponseDto.builder().id("1").name("Computers").status("INACTIVE").build()
        );

        CategoryResponseDto result = categoryService.toggleCategoryStatus(1L);

        assertThat(category.getActive()).isFalse();
        assertThat(result.getStatus()).isEqualTo("INACTIVE");
    }
}
