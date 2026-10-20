package com.zycus.stockpulse.service;

import com.zycus.stockpulse.entity.TriggerReason;
import com.zycus.stockpulse.event.ProductChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ProductChangeEventHandler {
    
    @Autowired
    private SuggestionService suggestionService;
    
    @EventListener
    @Async("taskExecutor")
    public void handleProductChangeEvent(ProductChangeEvent event) {
        try {
            // Generate both pricing and reorder suggestions asynchronously
            suggestionService.createPricingSuggestion(event.getProduct().getId(), event.getTriggerReason());
            suggestionService.createReorderSuggestion(event.getProduct().getId(), event.getTriggerReason());
        } catch (Exception e) {
            System.err.println("Error processing product change event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}