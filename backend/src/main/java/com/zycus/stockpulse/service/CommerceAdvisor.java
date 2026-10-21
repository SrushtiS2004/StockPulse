package com.zycus.stockpulse.service;

import com.zycus.stockpulse.entity.PricingSuggestion;
import com.zycus.stockpulse.entity.Product;
import com.zycus.stockpulse.entity.ReorderSuggestion;
import com.zycus.stockpulse.entity.TriggerReason;

public interface CommerceAdvisor {
    
    PricingSuggestion generatePricingSuggestion(Product product, TriggerReason triggerReason);
    
    ReorderSuggestion generateReorderSuggestion(Product product, TriggerReason triggerReason);
    
    String getStrategyName();
}