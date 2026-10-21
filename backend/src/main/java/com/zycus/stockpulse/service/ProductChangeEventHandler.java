package com.zycus.stockpulse.service;

import com.zycus.stockpulse.entity.TriggerReason;
import com.zycus.stockpulse.event.ProductChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ProductChangeEventHandler {
    
    private static final Logger log = LoggerFactory.getLogger(ProductChangeEventHandler.class);
    
    @Autowired
    private SuggestionService suggestionService;
    
    @EventListener
    @Async("taskExecutor")
    public void handleProductChangeEvent(ProductChangeEvent event) {
        Long productId = event.getProduct().getId();
        TriggerReason triggerReason = event.getTriggerReason();
        
        log.info("Agentic Loop Activated: Processing async recommendation for Product ID {} (SKU: {}) with trigger: {}",
                 productId, event.getProduct().getSku(), triggerReason);
        
        try {
            // Asynchronously generate both pricing and reorder suggestions
            suggestionService.createPricingSuggestion(productId, triggerReason);
            suggestionService.createReorderSuggestion(productId, triggerReason);
            log.info("Agentic Loop Completed: Generated suggestions for Product ID {}", productId);
        } catch (Exception e) {
            log.error("Error in async agentic recommendation loop for Product ID {}: {}", productId, e.getMessage(), e);
        }
    }
}