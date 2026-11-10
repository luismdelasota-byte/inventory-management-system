package com.inventory.modules.movements.service;

import com.inventory.common.exception.BadRequestException;
import com.inventory.common.exception.InsufficientStockException;
import com.inventory.modules.movements.dto.MovementItemRequestDto;
import com.inventory.modules.movements.dto.MovementRequestDto;
import com.inventory.modules.movements.dto.MovementResponseDto;
import com.inventory.modules.movements.entity.MovementType;
import com.inventory.modules.movements.entity.StockMovement;
import com.inventory.modules.movements.mapper.MovementMapper;
import com.inventory.modules.movements.repository.StockMovementRepository;
import com.inventory.modules.products.entity.Product;
import com.inventory.modules.products.repository.ProductRepository;
import com.inventory.modules.users.entity.Role;
import com.inventory.modules.users.entity.User;
import com.inventory.modules.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovementServiceTest {

    @Mock
    private StockMovementRepository movementRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MovementMapper movementMapper;

    @InjectMocks
    private MovementServiceImpl movementService;

    private User operatorUser;
    private Product product;

    @BeforeEach
    void setUp() {
        Role userRole = Role.builder().id(2L).name("INVENTORY_USER").build();
        operatorUser = User.builder()
                .id(2L)
                .fullName("Jamie Rivera")
                .username("jamie.rivera")
                .email("operator@inventory.com")
                .active(true)
                .role(userRole)
                .build();

        product = Product.builder()
                .id(1L)
                .sku("LAP-HP-002")
                .name("HP ProBook 450")
                .stockQuantity(10)
                .minimumStock(5)
                .purchasePrice(BigDecimal.valueOf(700))
                .salePrice(BigDecimal.valueOf(950))
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Record stock entry (IN) increases stock")
    void createMovement_IN_IncreasesStock() {
        MovementItemRequestDto itemDto = new MovementItemRequestDto(1L, 5);
        MovementRequestDto request = new MovementRequestDto("Supplier delivery", List.of(itemDto));

        when(userRepository.findByUsername("jamie.rivera")).thenReturn(Optional.of(operatorUser));
        when(movementRepository.count()).thenReturn(0L);
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MovementResponseDto expectedDto = MovementResponseDto.builder()
                .code("MOV-0001")
                .type("IN")
                .userName("Jamie Rivera")
                .reason("Supplier delivery")
                .build();
        when(movementMapper.toResponseDto(any(StockMovement.class))).thenReturn(expectedDto);

        MovementResponseDto result = movementService.createMovement(MovementType.IN, request, "jamie.rivera");

        assertThat(result).isNotNull();
        assertThat(product.getStockQuantity()).isEqualTo(15);
        verify(productRepository).save(product);
        verify(movementRepository).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("Record stock exit (OUT) decreases stock")
    void createMovement_OUT_DecreasesStock() {
        MovementItemRequestDto itemDto = new MovementItemRequestDto(1L, 4);
        MovementRequestDto request = new MovementRequestDto("Office deployment", List.of(itemDto));

        when(userRepository.findByUsername("jamie.rivera")).thenReturn(Optional.of(operatorUser));
        when(movementRepository.count()).thenReturn(1L);
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MovementResponseDto expectedDto = MovementResponseDto.builder()
                .code("MOV-0002")
                .type("OUT")
                .userName("Jamie Rivera")
                .build();
        when(movementMapper.toResponseDto(any(StockMovement.class))).thenReturn(expectedDto);

        MovementResponseDto result = movementService.createMovement(MovementType.OUT, request, "jamie.rivera");

        assertThat(result).isNotNull();
        assertThat(product.getStockQuantity()).isEqualTo(6);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Record stock exit (OUT) with quantity exceeding available stock throws InsufficientStockException")
    void createMovement_OUT_InsufficientStock_ThrowsException() {
        MovementItemRequestDto itemDto = new MovementItemRequestDto(1L, 15); // Available is 10
        MovementRequestDto request = new MovementRequestDto("Large order", List.of(itemDto));

        when(userRepository.findByUsername("jamie.rivera")).thenReturn(Optional.of(operatorUser));
        when(movementRepository.count()).thenReturn(1L);
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> movementService.createMovement(MovementType.OUT, request, "jamie.rivera"))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock for product");

        // Verify product was NOT saved with negative or changed stock
        assertThat(product.getStockQuantity()).isEqualTo(10);
        verify(movementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("Movement with inactive product throws BadRequestException")
    void createMovement_InactiveProduct_ThrowsBadRequest() {
        product.setActive(false);
        MovementItemRequestDto itemDto = new MovementItemRequestDto(1L, 2);
        MovementRequestDto request = new MovementRequestDto("Delivery", List.of(itemDto));

        when(userRepository.findByUsername("jamie.rivera")).thenReturn(Optional.of(operatorUser));
        when(movementRepository.count()).thenReturn(1L);
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> movementService.createMovement(MovementType.IN, request, "jamie.rivera"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Product is inactive");
    }

    @Test
    @DisplayName("Movement with duplicate product IDs in same request throws BadRequestException")
    void createMovement_DuplicateProductsInRequest_ThrowsBadRequest() {
        MovementItemRequestDto item1 = new MovementItemRequestDto(1L, 2);
        MovementItemRequestDto item2 = new MovementItemRequestDto(1L, 3);
        MovementRequestDto request = new MovementRequestDto("Double item", List.of(item1, item2));

        when(userRepository.findByUsername("jamie.rivera")).thenReturn(Optional.of(operatorUser));

        assertThatThrownBy(() -> movementService.createMovement(MovementType.IN, request, "jamie.rivera"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Duplicate product ID found");
    }
}
