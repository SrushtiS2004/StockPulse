package com.zycus.stockpulse.event;

import com.zycus.stockpulse.entity.Product;
import com.zycus.stockpulse.entity.TriggerReason;

public class ProductChangeEvent {
    
    private final Product product;
    private final TriggerReason triggerReason;
    
    public ProductChangeEvent(Product product, TriggerReason triggerReason) {
        this.product = product;
        this.triggerReason = triggerReason;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public TriggerReason getTriggerReason() {
        return triggerReason;
    }
}