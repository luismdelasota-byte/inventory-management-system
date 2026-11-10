package com.inventory.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.modules.products.dto.CreateProductRequestDto;
import com.inventory.modules.products.service.ProductService;
import com.inventory.modules.users.dto.CreateUserRequestDto;
import com.inventory.modules.users.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("Unauthenticated request to protected endpoint returns 401 Unauthorized")
    void unauthenticatedAccess_Returns401() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(username = "operator", roles = {"INVENTORY_USER"})
    @DisplayName("INVENTORY_USER trying to create a product returns 403 Forbidden")
    void inventoryUser_CreateProduct_Returns403() throws Exception {
        CreateProductRequestDto request = new CreateProductRequestDto(
                "TEST-SKU", "Test Product", "Description",
                1L, BigDecimal.valueOf(100), BigDecimal.valueOf(150), 10, 5
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied: insufficient permissions"));
    }

    @Test
    @WithMockUser(username = "operator", roles = {"INVENTORY_USER"})
    @DisplayName("INVENTORY_USER trying to create a user returns 403 Forbidden")
    void inventoryUser_CreateUser_Returns403() throws Exception {
        CreateUserRequestDto request = new CreateUserRequestDto(
                "Test User", "test.user", "test@inventory.com", "Password123!", "INVENTORY_USER"
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "operator", roles = {"INVENTORY_USER"})
    @DisplayName("INVENTORY_USER can view products (GET /api/products) successfully")
    void inventoryUser_CanViewProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("ADMIN can access product creation endpoint without 403")
    void admin_CanCreateProduct() throws Exception {
        CreateProductRequestDto request = new CreateProductRequestDto(
                "TEST-SKU", "Test Product", "Description",
                1L, BigDecimal.valueOf(100), BigDecimal.valueOf(150), 10, 5
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
