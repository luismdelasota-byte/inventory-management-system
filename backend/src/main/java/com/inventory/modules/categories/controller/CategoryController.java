package com.inventory.modules.categories.controller;

import com.inventory.common.response.ApiResponse;
import com.inventory.common.response.PagedResponse;
import com.inventory.modules.categories.dto.CategoryResponseDto;
import com.inventory.modules.categories.dto.CreateCategoryRequestDto;
import com.inventory.modules.categories.dto.UpdateCategoryRequestDto;
import com.inventory.modules.categories.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Endpoints for category management")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "List all categories or get paginated categories")
    public ResponseEntity<?> getAllCategories(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        if (page != null) {
            PagedResponse<CategoryResponseDto> paged = categoryService.getAllCategories(search, PageRequest.of(page, size, Sort.by("name").ascending()));
            return ResponseEntity.ok(paged);
        }

        List<CategoryResponseDto> list = categoryService.getAllCategoriesList();
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", list));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active categories for dropdowns")
    public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> getActiveCategories() {
        List<CategoryResponseDto> list = categoryService.getActiveCategories();
        return ResponseEntity.ok(ApiResponse.success("Active categories retrieved successfully", list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoryById(@PathVariable Long id) {
        CategoryResponseDto category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", category));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new category (ADMIN only)")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> createCategory(@Valid @RequestBody CreateCategoryRequestDto request) {
        CategoryResponseDto created = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing category (ADMIN only)")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequestDto request) {
        CategoryResponseDto updated = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle category active status (ADMIN only)")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> toggleStatus(@PathVariable Long id) {
        CategoryResponseDto updated = categoryService.toggleCategoryStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Category status updated successfully", updated));
    }
}
