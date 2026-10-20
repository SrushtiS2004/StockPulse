package com.zycus.stockpulse.dto;

import com.zycus.stockpulse.entity.PriceDirection;
import com.zycus.stockpulse.entity.SuggestionStatus;
import com.zycus.stockpulse.entity.TriggerReason;
import java.math.BigDecimal;

public class PricingSuggestionDTO {
    
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal currentPrice;
    private BigDecimal recommendedPrice;
    private PriceDirection direction;
    private Double confidence;
    private String reasoning;
    private SuggestionStatus status;
    private TriggerReason triggerReason;
    
    // Constructors
    public PricingSuggestionDTO() {}
    
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
    
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }
    
    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }
    
    public BigDecimal getRecommendedPrice() {
        return recommendedPrice;
    }
    
    public void setRecommendedPrice(BigDecimal recommendedPrice) {
        this.recommendedPrice = recommendedPrice;
    }
    
    public PriceDirection getDirection() {
        return direction;
    }
    
    public void setDirection(PriceDirection direction) {
        this.direction = direction;
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