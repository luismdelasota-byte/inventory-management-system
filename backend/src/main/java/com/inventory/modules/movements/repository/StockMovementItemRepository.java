package com.inventory.modules.movements.repository;

import com.inventory.modules.movements.entity.StockMovementItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementItemRepository extends JpaRepository<StockMovementItem, Long> {
    List<StockMovementItem> findByMovementId(Long movementId);
    List<StockMovementItem> findByProductId(Long productId);
}
