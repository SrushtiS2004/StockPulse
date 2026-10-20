package com.zycus.stockpulse.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class UpdateStockRequest {
    
    @NotNull
    @PositiveOrZero
    private Integer stockLevel;
    
    // Constructors
    public UpdateStockRequest() {}
    
    public UpdateStockRequest(Integer stockLevel) {
        this.stockLevel = stockLevel;
    }
    
    // Getters and Setters
    public Integer getStockLevel() {
        return stockLevel;
    }
    
    public void setStockLevel(Integer stockLevel) {
        this.stockLevel = stockLevel;
    }
}