package com.zycus.stockpulse.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reorder_suggestions")
public class ReorderSuggestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false)
    private Integer currentStock;
    
    @Column(nullable = false)
    private Integer recommendedQuantity;
    
    @Column(nullable = false)
    private Integer suggestedLeadTime;
    
    @Column(nullable = false)
    private Double confidence;
    
    @Column(length = 2000, nullable = false)
    private String reasoning;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SuggestionStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TriggerReason triggerReason;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public ReorderSuggestion() {
        this.createdAt = LocalDateTime.now();
        this.status = SuggestionStatus.PENDING;
    }
    
    public ReorderSuggestion(Product product, Integer currentStock, Integer recommendedQuantity, 
                             Integer suggestedLeadTime, Double confidence, String reasoning, TriggerReason triggerReason) {
        this.product = product;
        this.currentStock = currentStock;
        this.recommendedQuantity = recommendedQuantity;
        this.suggestedLeadTime = suggestedLeadTime;
        this.confidence = confidence;
        this.reasoning = reasoning;
        this.triggerReason = triggerReason;
        this.status = SuggestionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public Integer getCurrentStock() {
        return currentStock;
    }
    
    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }
    
    public Integer getRecommendedQuantity() {
        return recommendedQuantity;
    }
    
    public void setRecommendedQuantity(Integer recommendedQuantity) {
        this.recommendedQuantity = recommendedQuantity;
    }
    
    public Integer getSuggestedLeadTime() {
        return suggestedLeadTime;
    }
    
    public void setSuggestedLeadTime(Integer suggestedLeadTime) {
        this.suggestedLeadTime = suggestedLeadTime;
    }
    
    public Double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
    
    public String getReasoning() {
        return reasoning;
    }
    
    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }
    
    public SuggestionStatus getStatus() {
        return status;
    }
    
    public void setStatus(SuggestionStatus status) {
        this.status = status;
    }
    
    public TriggerReason getTriggerReason() {
        return triggerReason;
    }
    
    public void setTriggerReason(TriggerReason triggerReason) {
        this.triggerReason = triggerReason;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}