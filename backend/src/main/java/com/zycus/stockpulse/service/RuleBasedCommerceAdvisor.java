package com.zycus.stockpulse.service;

import com.zycus.stockpulse.entity.*;
import com.zycus.stockpulse.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class RuleBasedCommerceAdvisor implements CommerceAdvisor {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Override
    public PricingSuggestion generatePricingSuggestion(Product product) {
        // Calculate category average demand velocity
        Double categoryAvgVelocity = productRepository.findAverageDemandVelocityByCategory(product.getCategory());
        if (categoryAvgVelocity == null) {
            categoryAvgVelocity = 0.0;
        }
        
        BigDecimal currentPrice = product.getCurrentPrice();
        BigDecimal recommendedPrice = currentPrice;
        PriceDirection direction = PriceDirection.HOLD;
        double confidence = 0.8; // High confidence for rule-based
        String reasoning = "Rule-based pricing recommendation";
        TriggerReason triggerReason = TriggerReason.MANUAL;
        
        // Rule 1: If stock is below reorder threshold, recommend 10% price increase
        if (product.getStockLevel() <= product.getReorderThreshold()) {
            recommendedPrice = currentPrice.multiply(new BigDecimal("1.10")).setScale(2, RoundingMode.HALF_UP);
            direction = PriceDirection.INCREASE;
            confidence = 0.9;
            reasoning = "Stock level (" + product.getStockLevel() + ") is below reorder threshold (" + 
                       product.getReorderThreshold() + "). Recommending 10% price increase to preserve inventory.";
            triggerReason = TriggerReason.INVENTORY_LOW;
        }
        // Rule 2: If demand velocity is greater than twice the category average, recommend 5% increase
        else if (product.getDemandVelocity() > categoryAvgVelocity * 2 && categoryAvgVelocity > 0) {
            recommendedPrice = currentPrice.multiply(new BigDecimal("1.05")).setScale(2, RoundingMode.HALF_UP);
            direction = PriceDirection.INCREASE;
            confidence = 0.85;
            reasoning = "Demand velocity (" + product.getDemandVelocity() + ") is " + 
                       String.format("%.1f", (double)product.getDemandVelocity() / categoryAvgVelocity) + 
                       "x category average (" + String.format("%.1f", categoryAvgVelocity) + "). Recommending 5% price increase.";
            triggerReason = TriggerReason.DEMAND_SPIKE;
        }
        // Rule 3: Otherwise, hold the price
        else {
            direction = PriceDirection.HOLD;
            confidence = 0.7;
            reasoning = "No significant factors detected. Maintaining current price.";
            triggerReason = TriggerReason.INITIAL;
        }
        
        return new PricingSuggestion(product, currentPrice, recommendedPrice, direction, confidence, reasoning, triggerReason);
    }
    
    @Override
    public ReorderSuggestion generateReorderSuggestion(Product product) {
        int currentStock = product.getStockLevel();
        int reorderThreshold = product.getReorderThreshold();
        
        // Rule-based reorder quantity: max(1, reorderThreshold * 3 - currentStock)
        int recommendedQuantity = Math.max(1, reorderThreshold * 3 - currentStock);
        int suggestedLeadTime = 7; // Default lead time in days
        double confidence = 0.8;
        String reasoning = "Rule-based reorder recommendation";
        TriggerReason triggerReason = TriggerReason.MANUAL;
        
        // Adjust based on trigger conditions
        if (currentStock <= reorderThreshold) {
            confidence = 0.9;
            reasoning = "Current stock (" + currentStock + ") is at or below reorder threshold (" + 
                       reorderThreshold + "). Recommended reorder quantity: " + recommendedQuantity;
            triggerReason = TriggerReason.INVENTORY_LOW;
        } else {
            confidence = 0.7;
            reasoning = "Proactive reorder recommendation to maintain optimal inventory levels. " +
                       "Recommended quantity: " + recommendedQuantity;
            triggerReason = TriggerReason.INITIAL;
        }
        
        return new ReorderSuggestion(product, currentStock, recommendedQuantity, suggestedLeadTime, confidence, reasoning, triggerReason);
    }
}