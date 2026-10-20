package com.zycus.stockpulse.repository;

import com.zycus.stockpulse.entity.Product;
import com.zycus.stockpulse.entity.ProductStatus;
import com.zycus.stockpulse.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findBySku(String sku);
    
    List<Product> findByStatus(ProductStatus status);
    
    List<Product> findByCategory(Category category);
    
    List<Product> findByStatusAndCategory(ProductStatus status, Category category);
    
    @Query("SELECT AVG(p.demandVelocity) FROM Product p WHERE p.category = :category")
    Double findAverageDemandVelocityByCategory(@Param("category") Category category);
    
    List<Product> findByStockLevelLessThanEqual(Integer threshold);
    
    @Query("SELECT p FROM Product p WHERE p.stockLevel <= p.reorderThreshold AND p.status != 'OUT_OF_STOCK'")
    List<Product> findLowStockProducts();
}