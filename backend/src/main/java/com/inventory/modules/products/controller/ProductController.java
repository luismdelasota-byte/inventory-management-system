package com.inventory.modules.products.controller;

import com.inventory.common.response.ApiResponse;
import com.inventory.common.response.PagedResponse;
import com.inventory.modules.products.dto.CreateProductRequestDto;
import com.inventory.modules.products.dto.ProductResponseDto;
import com.inventory.modules.products.dto.UpdateProductRequestDto;
import com.inventory.modules.products.service.ProductService;
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
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Endpoints for product management")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "List all products or get filtered/paginated products")
    public ResponseEntity<?> getAllProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        if (page != null) {
            PagedResponse<ProductResponseDto> paged = productService.getAllProducts(search, categoryId, active, PageRequest.of(page, size, Sort.by("id").descending()));
            return ResponseEntity.ok(paged);
        }

        List<ProductResponseDto> list = productService.getAllProductsList(search);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProductById(@PathVariable Long id) {
        ProductResponseDto product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new product (ADMIN only)")
    public ResponseEntity<ApiResponse<ProductResponseDto>> createProduct(@Valid @RequestBody CreateProductRequestDto request) {
        ProductResponseDto created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing product (ADMIN only - stock cannot be modified here)")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequestDto request) {
        ProductResponseDto updated = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle product active status (ADMIN only)")
    public ResponseEntity<ApiResponse<ProductResponseDto>> toggleStatus(@PathVariable Long id) {
        ProductResponseDto updated = productService.toggleProductStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Product status updated successfully", updated));
    }
}
