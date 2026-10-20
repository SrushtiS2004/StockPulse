package com.zycus.stockpulse.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "reorder_suggestions")
public class ReorderSuggestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    private Integer currentStock;
    
    private Integer recommendedQuantity;
    
    private Integer suggestedLeadTime;
    
    @Column(scale = 2)
    private Double confidence;
    
    @Column(length = 1000)
    private String reasoning;
    
    @Enumerated(EnumType.STRING)
    private SuggestionStatus status;
    
    @Enumerated(EnumType.STRING)
    private TriggerReason triggerReason;
    
    // Constructors
    public ReorderSuggestion() {}
    
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
}