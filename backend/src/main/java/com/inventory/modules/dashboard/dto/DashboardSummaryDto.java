package com.inventory.modules.dashboard.dto;

import com.inventory.modules.movements.dto.MovementResponseDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDto {
    private long totalProducts;
    private long totalCategories;
    private long lowStockProducts;
    private long outOfStockProducts;
    private long totalMovements;
    private long entriesThisMonth;
    private long exitsThisMonth;
    private List<MovementResponseDto> recentMovements;
    private List<LowStockProductDto> lowStockItems;
}
