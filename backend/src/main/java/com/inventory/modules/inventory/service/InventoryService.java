package com.inventory.modules.inventory.service;

import com.inventory.common.response.PagedResponse;
import com.inventory.modules.inventory.dto.InventoryItemDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventoryService {
    List<InventoryItemDto> getInventoryList(String search);
    PagedResponse<InventoryItemDto> getInventory(String search, Long categoryId, String status, Pageable pageable);
    List<InventoryItemDto> getLowStockItems();
}
