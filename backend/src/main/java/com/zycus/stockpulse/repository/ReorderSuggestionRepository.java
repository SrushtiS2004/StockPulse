package com.zycus.stockpulse.repository;

import com.zycus.stockpulse.entity.Product;
import com.zycus.stockpulse.entity.ReorderSuggestion;
import com.zycus.stockpulse.entity.SuggestionStatus;
import com.zycus.stockpulse.entity.TriggerReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReorderSuggestionRepository extends JpaRepository<ReorderSuggestion, Long> {
    
    List<ReorderSuggestion> findByProductId(Long productId);
    
    List<ReorderSuggestion> findByStatusOrderByCreatedAtDesc(SuggestionStatus status);
    
    List<ReorderSuggestion> findByProductAndStatus(Product product, SuggestionStatus status);
    
    @Query("SELECT rs FROM ReorderSuggestion rs WHERE rs.product = :product AND rs.status = :status AND rs.triggerReason = :triggerReason")
    Optional<ReorderSuggestion> findByProductAndStatusAndTriggerReason(
            @Param("product") Product product, 
            @Param("status") SuggestionStatus status, 
            @Param("triggerReason") TriggerReason triggerReason);
    
    @Query("SELECT COUNT(rs) FROM ReorderSuggestion rs WHERE rs.product = :product AND rs.status = :status")
    long countByProductAndStatus(@Param("product") Product product, @Param("status") SuggestionStatus status);
}