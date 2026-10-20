package com.zycus.stockpulse.service;

import com.zycus.stockpulse.entity.Product;
import com.zycus.stockpulse.entity.TriggerReason;
import com.zycus.stockpulse.event.ProductChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class ProductEventPublisher {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    public void publishProductChange(Product product, TriggerReason triggerReason) {
        ProductChangeEvent event = new ProductChangeEvent(product, triggerReason);
        eventPublisher.publishEvent(event);
    }
}