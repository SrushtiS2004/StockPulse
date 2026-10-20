package com.zycus.stockpulse.service;

import com.zycus.stockpulse.entity.Product;
import com.zycus.stockpulse.entity.PricingSuggestion;
import com.zycus.stockpulse.entity.ReorderSuggestion;

public interface CommerceAdvisor {
    
    PricingSuggestion generatePricingSuggestion(Product product);
    
    ReorderSuggestion generateReorderSuggestion(Product product);
}