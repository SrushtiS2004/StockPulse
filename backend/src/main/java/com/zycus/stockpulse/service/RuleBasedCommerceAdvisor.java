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
    public String getStrategyName() {
        return "rule-based";
    }
    
    @Override
    public PricingSuggestion generatePricingSuggestion(Product product, TriggerReason triggerReason) {
        Double categoryAvgVelocity = productRepository != null 
                ? productRepository.findAverageDemandVelocityByCategory(product.getCategory()) 
                : 0.0;
        if (categoryAvgVelocity == null) {
            categoryAvgVelocity = 0.0;
        }
        
        BigDecimal currentPrice = product.getCurrentPrice();
        BigDecimal recommendedPrice = currentPrice;
        PriceDirection direction = PriceDirection.HOLD;
        double confidence = 0.75;
        String reasoning;
        
        // Rule 1: Stock is at or below threshold -> 10% increase to protect scarce inventory
        if (product.getStockLevel() <= product.getReorderThreshold()) {
            recommendedPrice = currentPrice.multiply(new BigDecimal("1.10")).setScale(2, RoundingMode.HALF_UP);
            direction = PriceDirection.INCREASE;
            confidence = 0.90;
            reasoning = String.format(
                "Rule Engine: Stock level (%d) is at or below reorder threshold (%d). Recommending 10%% price increase to protect remaining inventory while replenishment is processed.",
                product.getStockLevel(), product.getReorderThreshold()
            );
        }
        // Rule 2: Demand velocity is > 2x category average -> 5% increase to capture surplus value
        else if (categoryAvgVelocity > 0 && product.getDemandVelocity() > categoryAvgVelocity * 2) {
            recommendedPrice = currentPrice.multiply(new BigDecimal("1.05")).setScale(2, RoundingMode.HALF_UP);
            direction = PriceDirection.INCREASE;
            confidence = 0.85;
            reasoning = String.format(
                "Rule Engine: Demand velocity (%d/day) is %.1fx category peer average (%.1f/day). Recommending 5%% price increase to optimize margin during demand surge.",
                product.getDemandVelocity(), (product.getDemandVelocity() / categoryAvgVelocity), categoryAvgVelocity
            );
        }
        // Rule 3: Steady state -> HOLD current price
        else {
            recommendedPrice = currentPrice;
            direction = PriceDirection.HOLD;
            confidence = 0.80;
            reasoning = String.format(
                "Rule Engine: Inventory level (%d units) and velocity (%d/day) are within normal operational parameters. Maintaining current price.",
                product.getStockLevel(), product.getDemandVelocity()
            );
        }
        
        return new PricingSuggestion(product, currentPrice, recommendedPrice, direction, confidence, reasoning, triggerReason);
    }
    
    @Override
    public ReorderSuggestion generateReorderSuggestion(Product product, TriggerReason triggerReason) {
        int currentStock = product.getStockLevel();
        int threshold = product.getReorderThreshold();
        
        // Quantity = max(1, (threshold * 3) - currentStock)
        int recommendedQuantity = Math.max(1, (threshold * 3) - currentStock);
        
        int leadTimeDays = switch (product.getCategory()) {
            case ELECTRONICS -> 7;
            case APPAREL -> 5;
            case HOME -> 6;
        };
        
        double confidence = currentStock <= threshold ? 0.92 : 0.78;
        String reasoning;
        
        if (currentStock <= threshold) {
            reasoning = String.format(
                "Rule Engine: Stock (%d) breached reorder threshold (%d). Recommended baseline restock quantity is %d units (target 3x buffer) with estimated %d-day supplier lead time.",
                currentStock, threshold, recommendedQuantity, leadTimeDays
            );
        } else {
            reasoning = String.format(
                "Rule Engine: Proactive replenishment recommendation of %d units to maintain target buffer stock before peak depletion.",
                recommendedQuantity
            );
        }
        
        return new ReorderSuggestion(product, currentStock, recommendedQuantity, leadTimeDays, confidence, reasoning, triggerReason);
    }
}