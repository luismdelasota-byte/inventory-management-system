package com.inventory.modules.products.service;

import com.inventory.common.response.PagedResponse;
import com.inventory.modules.products.dto.CreateProductRequestDto;
import com.inventory.modules.products.dto.ProductResponseDto;
import com.inventory.modules.products.dto.UpdateProductRequestDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    List<ProductResponseDto> getAllProductsList(String search);
    PagedResponse<ProductResponseDto> getAllProducts(String search, Long categoryId, Boolean active, Pageable pageable);
    ProductResponseDto getProductById(Long id);
    ProductResponseDto createProduct(CreateProductRequestDto request);
    ProductResponseDto updateProduct(Long id, UpdateProductRequestDto request);
    ProductResponseDto toggleProductStatus(Long id);
}
