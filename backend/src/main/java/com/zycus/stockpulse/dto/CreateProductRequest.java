package com.zycus.stockpulse.dto;

import com.zycus.stockpulse.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public class CreateProductRequest {
    
    @NotBlank
    private String sku;
    
    @NotBlank
    private String name;
    
    @NotNull
    private Category category;
    
    @NotNull
    private BigDecimal currentPrice;
    
    private BigDecimal costPrice;
    
    @PositiveOrZero
    private Integer stockLevel;
    
    @PositiveOrZero
    private Integer reorderThreshold;
    
    @PositiveOrZero
    private Integer demandVelocity;
    
    // Constructors
    public CreateProductRequest() {}
    
    // Getters and Setters
    public String getSku() {
        return sku;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }
    
    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }
    
    public BigDecimal getCostPrice() {
        return costPrice;
    }
    
    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }
    
    public Integer getStockLevel() {
        return stockLevel;
    }
    
    public void setStockLevel(Integer stockLevel) {
        this.stockLevel = stockLevel;
    }
    
    public Integer getReorderThreshold() {
        return reorderThreshold;
    }
    
    public void setReorderThreshold(Integer reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }
    
    public Integer getDemandVelocity() {
        return demandVelocity;
    }
    
    public void setDemandVelocity(Integer demandVelocity) {
        this.demandVelocity = demandVelocity;
    }
}