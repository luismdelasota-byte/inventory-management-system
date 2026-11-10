package com.inventory.modules.inventory.controller;

import com.inventory.common.response.ApiResponse;
import com.inventory.common.response.PagedResponse;
import com.inventory.modules.inventory.dto.InventoryItemDto;
import com.inventory.modules.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Endpoints for inventory tracking and stock alerts")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @Operation(summary = "Get inventory items with stock status (AVAILABLE, LOW_STOCK, OUT_OF_STOCK)")
    public ResponseEntity<?> getInventory(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        if (page != null) {
            PagedResponse<InventoryItemDto> paged = inventoryService.getInventory(search, categoryId, status, PageRequest.of(page, size, Sort.by("id").descending()));
            return ResponseEntity.ok(paged);
        }

        List<InventoryItemDto> list = inventoryService.getInventoryList(search);
        return ResponseEntity.ok(ApiResponse.success("Inventory retrieved successfully", list));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get items that have low stock or are out of stock")
    public ResponseEntity<ApiResponse<List<InventoryItemDto>>> getLowStockItems() {
        List<InventoryItemDto> lowStockList = inventoryService.getLowStockItems();
        return ResponseEntity.ok(ApiResponse.success("Low stock items retrieved successfully", lowStockList));
    }
}
