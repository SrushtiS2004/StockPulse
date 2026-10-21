package com.zycus.stockpulse.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pricing_suggestions")
public class PricingSuggestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal currentPrice;
    
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal recommendedPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriceDirection direction;
    
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
    public PricingSuggestion() {
        this.createdAt = LocalDateTime.now();
        this.status = SuggestionStatus.PENDING;
    }
    
    public PricingSuggestion(Product product, BigDecimal currentPrice, BigDecimal recommendedPrice, 
                           PriceDirection direction, Double confidence, String reasoning, TriggerReason triggerReason) {
        this.product = product;
        this.currentPrice = currentPrice;
        this.recommendedPrice = recommendedPrice;
        this.direction = direction;
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}