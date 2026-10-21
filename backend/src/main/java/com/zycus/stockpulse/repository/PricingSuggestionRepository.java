package com.zycus.stockpulse.repository;

import com.zycus.stockpulse.entity.PricingSuggestion;
import com.zycus.stockpulse.entity.Product;
import com.zycus.stockpulse.entity.SuggestionStatus;
import com.zycus.stockpulse.entity.TriggerReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingSuggestionRepository extends JpaRepository<PricingSuggestion, Long> {
    
    List<PricingSuggestion> findByProductId(Long productId);
    
    List<PricingSuggestion> findByStatusOrderByCreatedAtDesc(SuggestionStatus status);
    
    List<PricingSuggestion> findByProductAndStatus(Product product, SuggestionStatus status);
    
    @Query("SELECT ps FROM PricingSuggestion ps WHERE ps.product = :product AND ps.status = :status AND ps.triggerReason = :triggerReason")
    Optional<PricingSuggestion> findByProductAndStatusAndTriggerReason(
            @Param("product") Product product, 
            @Param("status") SuggestionStatus status, 
            @Param("triggerReason") TriggerReason triggerReason);
    
    @Query("SELECT COUNT(ps) FROM PricingSuggestion ps WHERE ps.product = :product AND ps.status = :status")
    long countByProductAndStatus(@Param("product") Product product, @Param("status") SuggestionStatus status);
}