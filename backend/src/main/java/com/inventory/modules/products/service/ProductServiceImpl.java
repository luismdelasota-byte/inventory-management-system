package com.inventory.modules.products.service;

import com.inventory.common.exception.BadRequestException;
import com.inventory.common.exception.ConflictException;
import com.inventory.common.exception.ResourceNotFoundException;
import com.inventory.common.response.PagedResponse;
import com.inventory.modules.categories.entity.Category;
import com.inventory.modules.categories.repository.CategoryRepository;
import com.inventory.modules.products.dto.CreateProductRequestDto;
import com.inventory.modules.products.dto.ProductResponseDto;
import com.inventory.modules.products.dto.UpdateProductRequestDto;
import com.inventory.modules.products.entity.Product;
import com.inventory.modules.products.mapper.ProductMapper;
import com.inventory.modules.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProductsList(String search) {
        List<Product> products = productRepository.findAllWithCategory(search);
        return products.stream()
                .map(productMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponseDto> getAllProducts(String search, Long categoryId, Boolean active, Pageable pageable) {
        Page<Product> productsPage = productRepository.findAllWithFilters(search, categoryId, active, pageable);
        List<ProductResponseDto> dtos = productsPage.getContent().stream()
                .map(productMapper::toResponseDto)
                .collect(Collectors.toList());

        return PagedResponse.<ProductResponseDto>builder()
                .success(true)
                .message("Products retrieved successfully")
                .data(dtos)
                .page(productsPage.getNumber())
                .size(productsPage.getSize())
                .totalElements(productsPage.getTotalElements())
                .totalPages(productsPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productMapper.toResponseDto(product);
    }

    @Override
    @Transactional
    public ProductResponseDto createProduct(CreateProductRequestDto request) {
        String trimmedSku = request.getSku().trim().toUpperCase();
        if (productRepository.existsBySku(trimmedSku)) {
            throw new ConflictException("Product SKU already exists: " + trimmedSku);
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new BadRequestException("Cannot create a product in an inactive category.");
        }

        Product product = Product.builder()
                .sku(trimmedSku)
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .category(category)
                .purchasePrice(request.getPurchasePrice())
                .salePrice(request.getSalePrice())
                .stockQuantity(request.getInitialStock() != null ? request.getInitialStock() : 0)
                .minimumStock(request.getMinimumStock() != null ? request.getMinimumStock() : 0)
                .active(true)
                .build();

        Product saved = productRepository.save(product);
        return productMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public ProductResponseDto updateProduct(Long id, UpdateProductRequestDto request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        String trimmedSku = request.getSku().trim().toUpperCase();
        if (!product.getSku().equalsIgnoreCase(trimmedSku) && productRepository.existsBySku(trimmedSku)) {
            throw new ConflictException("Product SKU already exists: " + trimmedSku);
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new BadRequestException("Cannot assign a product to an inactive category.");
        }

        product.setSku(trimmedSku);
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        product.setCategory(category);
        product.setPurchasePrice(request.getPurchasePrice());
        product.setSalePrice(request.getSalePrice());
        product.setMinimumStock(request.getMinimumStock());

        // Note: stockQuantity is strictly preserved and not modified here.

        Product updated = productRepository.save(product);
        return productMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public ProductResponseDto toggleProductStatus(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setActive(!Boolean.TRUE.equals(product.getActive()));
        Product updated = productRepository.save(product);
        return productMapper.toResponseDto(updated);
    }
}
