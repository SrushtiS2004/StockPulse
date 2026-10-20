package com.zycus.stockpulse.dto;

import com.zycus.stockpulse.entity.SuggestionStatus;

public class UpdateSuggestionRequest {
    
    private SuggestionStatus status;
    
    // Constructors
    public UpdateSuggestionRequest() {}
    
    public UpdateSuggestionRequest(SuggestionStatus status) {
        this.status = status;
    }
    
    // Getters and Setters
    public SuggestionStatus getStatus() {
        return status;
    }
    
    public void setStatus(SuggestionStatus status) {
        this.status = status;
    }
}