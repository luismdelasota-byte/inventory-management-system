package com.inventory.modules.movements.repository;

import com.inventory.modules.movements.entity.MovementType;
import com.inventory.modules.movements.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query("SELECT DISTINCT m FROM StockMovement m JOIN FETCH m.user LEFT JOIN FETCH m.items i LEFT JOIN FETCH i.product WHERE m.id = :id")
    Optional<StockMovement> findByIdWithDetails(@Param("id") Long id);

    @Query(value = "SELECT m FROM StockMovement m JOIN FETCH m.user WHERE " +
           "(:type IS NULL OR m.movementType = :type) AND " +
           "(:search IS NULL OR :search = '' OR LOWER(m.code) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(m.reason) LIKE LOWER(CONCAT('%', :search, '%')))",
           countQuery = "SELECT COUNT(m) FROM StockMovement m WHERE " +
           "(:type IS NULL OR m.movementType = :type) AND " +
           "(:search IS NULL OR :search = '' OR LOWER(m.code) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(m.reason) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<StockMovement> findAllWithFilters(@Param("type") MovementType type,
                                            @Param("search") String search,
                                            Pageable pageable);

    @Query("SELECT DISTINCT m FROM StockMovement m JOIN FETCH m.user LEFT JOIN FETCH m.items i LEFT JOIN FETCH i.product ORDER BY m.createdAt DESC")
    List<StockMovement> findRecentMovements(Pageable pageable);

    long countByMovementTypeAndCreatedAtBetween(MovementType type, LocalDateTime start, LocalDateTime end);

    long count();
}
