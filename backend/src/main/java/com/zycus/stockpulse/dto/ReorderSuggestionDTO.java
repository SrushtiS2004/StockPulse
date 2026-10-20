package com.zycus.stockpulse.dto;

import com.zycus.stockpulse.entity.SuggestionStatus;
import com.zycus.stockpulse.entity.TriggerReason;

public class ReorderSuggestionDTO {
    
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Integer currentStock;
    private Integer recommendedQuantity;
    private Integer suggestedLeadTime;
    private Double confidence;
    private String reasoning;
    private SuggestionStatus status;
    private TriggerReason triggerReason;
    
    // Constructors
    public ReorderSuggestionDTO() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getProductSku() {
        return productSku;
    }
    
    public void setProductSku(String productSku) {
        this.productSku = productSku;
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