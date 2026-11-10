package com.inventory.modules.products.service;

import com.inventory.common.exception.BadRequestException;
import com.inventory.common.exception.ConflictException;
import com.inventory.modules.categories.entity.Category;
import com.inventory.modules.categories.repository.CategoryRepository;
import com.inventory.modules.products.dto.CreateProductRequestDto;
import com.inventory.modules.products.dto.ProductResponseDto;
import com.inventory.modules.products.dto.UpdateProductRequestDto;
import com.inventory.modules.products.entity.Product;
import com.inventory.modules.products.mapper.ProductMapper;
import com.inventory.modules.products.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        category = Category.builder().id(1L).name("Computers").active(true).build();
        product = Product.builder()
                .id(1L)
                .sku("LAP-LEN-001")
                .name("Lenovo ThinkPad E14")
                .category(category)
                .purchasePrice(BigDecimal.valueOf(680))
                .salePrice(BigDecimal.valueOf(899))
                .stockQuantity(24)
                .minimumStock(8)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Create product successfully")
    void createProduct_Success() {
        CreateProductRequestDto request = new CreateProductRequestDto(
                "LAP-LEN-001", "Lenovo ThinkPad E14", "Business laptop",
                1L, BigDecimal.valueOf(680), BigDecimal.valueOf(899), 24, 8
        );

        when(productRepository.existsBySku("LAP-LEN-001")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponseDto(product)).thenReturn(
                ProductResponseDto.builder().id("1").sku("LAP-LEN-001").name("Lenovo ThinkPad E14").stock(24).build()
        );

        ProductResponseDto result = productService.createProduct(request);

        assertThat(result).isNotNull();
        assertThat(result.getSku()).isEqualTo("LAP-LEN-001");
        assertThat(result.getStock()).isEqualTo(24);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Create product with duplicate SKU throws ConflictException")
    void createProduct_DuplicateSku_ThrowsConflict() {
        CreateProductRequestDto request = new CreateProductRequestDto(
                "LAP-LEN-001", "Lenovo ThinkPad E14", "Business laptop",
                1L, BigDecimal.valueOf(680), BigDecimal.valueOf(899), 24, 8
        );

        when(productRepository.existsBySku("LAP-LEN-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Product SKU already exists");
    }

    @Test
    @DisplayName("Create product in inactive category throws BadRequestException")
    void createProduct_InactiveCategory_ThrowsBadRequest() {
        category.setActive(false);
        CreateProductRequestDto request = new CreateProductRequestDto(
                "LAP-LEN-001", "Lenovo ThinkPad E14", "Business laptop",
                1L, BigDecimal.valueOf(680), BigDecimal.valueOf(899), 24, 8
        );

        when(productRepository.existsBySku("LAP-LEN-001")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot create a product in an inactive category");
    }

    @Test
    @DisplayName("Update product preserves existing stockQuantity")
    void updateProduct_PreservesStockQuantity() {
        UpdateProductRequestDto request = new UpdateProductRequestDto(
                "LAP-LEN-001", "Lenovo ThinkPad E14 Gen 2", "Updated description",
                1L, BigDecimal.valueOf(700), BigDecimal.valueOf(950), 10
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponseDto(product)).thenReturn(
                ProductResponseDto.builder().id("1").sku("LAP-LEN-001").name("Lenovo ThinkPad E14 Gen 2").stock(24).build()
        );

        ProductResponseDto result = productService.updateProduct(1L, request);

        assertThat(result).isNotNull();
        // Crucial requirement: stock quantity remains 24, untouched by update
        assertThat(product.getStockQuantity()).isEqualTo(24);
        verify(productRepository).save(product);
    }
}
