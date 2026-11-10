package com.inventory.modules.products.repository;

import com.inventory.modules.products.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    boolean existsByCategoryId(Long categoryId);

    Optional<Product> findBySku(String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    long countByActiveTrue();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true AND p.stockQuantity = 0")
    long countOutOfStock();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true AND p.stockQuantity > 0 AND p.stockQuantity <= p.minimumStock")
    long countLowStock();

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.active = true AND p.stockQuantity > 0 AND p.stockQuantity <= p.minimumStock")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.active = true AND (p.stockQuantity = 0 OR (p.stockQuantity > 0 AND p.stockQuantity <= p.minimumStock))")
    List<Product> findLowOrOutOfStockProducts();

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:active IS NULL OR p.active = :active)")
    Page<Product> findAllWithFilters(@Param("search") String search,
                                     @Param("categoryId") Long categoryId,
                                     @Param("active") Boolean active,
                                     Pageable pageable);

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Product> findAllWithCategory(@Param("search") String search);
}
