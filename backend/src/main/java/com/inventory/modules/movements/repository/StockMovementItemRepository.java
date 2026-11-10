package com.inventory.modules.movements.repository;

import com.inventory.modules.movements.entity.StockMovementItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementItemRepository extends JpaRepository<StockMovementItem, Long> {
    List<StockMovementItem> findByMovementId(Long movementId);
    List<StockMovementItem> findByProductId(Long productId);
}
