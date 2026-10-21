package com.zycus.stockpulse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zycus.stockpulse.entity.*;
import com.zycus.stockpulse.repository.ProductRepository;
import com.zycus.stockpulse.service.ai.LlmGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class AICommerceAdvisor implements CommerceAdvisor {
    
    private static final Logger log = LoggerFactory.getLogger(AICommerceAdvisor.class);
    
    @Autowired
    private LlmGateway llmGateway;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private RuleBasedCommerceAdvisor fallbackAdvisor;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String getStrategyName() {
        return "ai";
    }
    
    @Override
    public PricingSuggestion generatePricingSuggestion(Product product, TriggerReason triggerReason) {
        try {
            Double categoryAvg = productRepository != null 
                    ? productRepository.findAverageDemandVelocityByCategory(product.getCategory()) 
                    : 0.0;
            if (categoryAvg == null) categoryAvg = 0.0;
            
            String prompt = buildPricingPrompt(product, triggerReason, categoryAvg);
            String rawJson = llmGateway.callLlmRaw(prompt);
            
            if (rawJson != null && !rawJson.isBlank()) {
                JsonNode root = objectMapper.readTree(cleanJsonString(rawJson));
                
                BigDecimal recommendedPrice = parsePrice(root, product.getCurrentPrice());
                PriceDirection direction = parseDirection(root, recommendedPrice, product.getCurrentPrice());
                Double confidence = parseConfidence(root);
                String reasoning = parseReasoning(root, "AI Commerce Advisor: Dynamic pricing recommendation generated based on inventory and velocity analysis.");
                
                return new PricingSuggestion(
                    product,
                    product.getCurrentPrice(),
                    recommendedPrice,
                    direction,
                    confidence,
                    reasoning,
                    triggerReason
                );
            }
        } catch (Exception e) {
            log.warn("AI pricing recommendation generation failed: {}. Falling back to rule-based advisor.", e.getMessage());
        }
        
        // Resilient fallback
        return fallbackAdvisor.generatePricingSuggestion(product, triggerReason);
    }
    
    @Override
    public ReorderSuggestion generateReorderSuggestion(Product product, TriggerReason triggerReason) {
        try {
            Double categoryAvg = productRepository != null 
                    ? productRepository.findAverageDemandVelocityByCategory(product.getCategory()) 
                    : 0.0;
            if (categoryAvg == null) categoryAvg = 0.0;
            
            String prompt = buildReorderPrompt(product, triggerReason, categoryAvg);
            String rawJson = llmGateway.callLlmRaw(prompt);
            
            if (rawJson != null && !rawJson.isBlank()) {
                JsonNode root = objectMapper.readTree(cleanJsonString(rawJson));
                
                Integer quantity = parseQuantity(root, product.getReorderThreshold(), product.getStockLevel());
                Integer leadTime = parseLeadTime(root);
                Double confidence = parseConfidence(root);
                String reasoning = parseReasoning(root, "AI Commerce Advisor: Automated replenishment suggestion based on supply velocity.");
                
                return new ReorderSuggestion(
                    product,
                    product.getStockLevel(),
                    quantity,
                    leadTime,
                    confidence,
                    reasoning,
                    triggerReason
                );
            }
        } catch (Exception e) {
            log.warn("AI reorder recommendation generation failed: {}. Falling back to rule-based advisor.", e.getMessage());
        }
        
        // Resilient fallback
        return fallbackAdvisor.generateReorderSuggestion(product, triggerReason);
    }
    
    private String cleanJsonString(String raw) {
        String clean = raw.trim();
        if (clean.startsWith("```json")) {
            clean = clean.substring(7);
        } else if (clean.startsWith("```")) {
            clean = clean.substring(3);
        }
        if (clean.endsWith("```")) {
            clean = clean.substring(0, clean.length() - 3);
        }
        return clean.trim();
    }
    
    private String buildPricingPrompt(Product product, TriggerReason triggerReason, double categoryAvg) {
        if (triggerReason == TriggerReason.INVENTORY_LOW) {
            return String.format("""
                You are ShopStream's AI Commerce Advisor evaluating an INVENTORY LOW trigger.
                Merchandising context:
                - Stock has dropped to %d units (at or below reorder threshold of %d).
                - Current Price: $%.2f. Demand Velocity: %d orders/day (Category '%s' average: %.1f orders/day).
                - Decision Dilemma: Decide whether to RAISE the price (5%%-20%%) to protect scarce inventory and slow stockout before restock arrives, or HOLD/DISCOUNT if velocity is stalled.
                
                Return JSON format:
                {
                  "recommendedPrice": 29.99,
                  "direction": "INCREASE" | "DECREASE" | "HOLD",
                  "confidence": 0.85,
                  "reasoning": "Clear explanation of the inventory protection rationale"
                }
                """, product.getStockLevel(), product.getReorderThreshold(), product.getCurrentPrice(),
                product.getDemandVelocity(), product.getCategory().name(), categoryAvg);
        } else if (triggerReason == TriggerReason.DEMAND_SPIKE) {
            return String.format("""
                You are ShopStream's AI Commerce Advisor evaluating a DEMAND SPIKE trigger.
                Merchandising context:
                - Product '%s' (%s) is experiencing viral demand with %d orders/day (Category peer average is %.1f/day).
                - Current Price: $%.2f. Current Stock: %d units (Reorder threshold: %d).
                - Decision Dilemma: Recommend a strategic price increase (e.g. 5%%-15%%) to capture consumer surplus without dampening conversion momentum.
                
                Return JSON format:
                {
                  "recommendedPrice": 29.99,
                  "direction": "INCREASE" | "HOLD",
                  "confidence": 0.90,
                  "reasoning": "Clear explanation of the surge capitalization rationale"
                }
                """, product.getName(), product.getCategory().name(), product.getDemandVelocity(),
                categoryAvg, product.getCurrentPrice(), product.getStockLevel(), product.getReorderThreshold());
        } else {
            return String.format("""
                You are ShopStream's AI Commerce Advisor performing an on-demand pricing evaluation.
                Product Details:
                - SKU: %s, Name: %s, Category: %s
                - Current Price: $%.2f, Stock: %d, Reorder Threshold: %d, Demand: %d/day (Category avg: %.1f/day)
                
                Return JSON format:
                {
                  "recommendedPrice": 29.99,
                  "direction": "INCREASE" | "DECREASE" | "HOLD",
                  "confidence": 0.82,
                  "reasoning": "Concise reasoning for merchandising approval"
                }
                """, product.getSku(), product.getName(), product.getCategory().name(),
                product.getCurrentPrice(), product.getStockLevel(), product.getReorderThreshold(),
                product.getDemandVelocity(), categoryAvg);
        }
    }
    
    private String buildReorderPrompt(Product product, TriggerReason triggerReason, double categoryAvg) {
        return String.format("""
            You are ShopStream's AI Replenishment Advisor.
            Context: Product '%s' (%s), Current Stock: %d units, Reorder Threshold: %d, Demand Velocity: %d orders/day.
            Trigger Reason: %s. Category average velocity: %.1f/day.
            
            Recommend an optimal batch replenishment quantity and estimated supplier lead time in days.
            Return JSON format:
            {
              "recommendedQuantity": 50,
              "suggestedLeadTime": 7,
              "confidence": 0.88,
              "reasoning": "Clear supply chain rationale explaining buffer calculation"
            }
            """, product.getName(), product.getCategory().name(), product.getStockLevel(),
            product.getReorderThreshold(), product.getDemandVelocity(), triggerReason.name(), categoryAvg);
    }
    
    // Bounds Validation & Parsers
    private BigDecimal parsePrice(JsonNode root, BigDecimal fallback) {
        try {
            if (root.has("recommendedPrice") && !root.get("recommendedPrice").isNull()) {
                BigDecimal p = new BigDecimal(root.get("recommendedPrice").asText());
                // Sanity check: must be positive and between 0.2x and 4x of current price
                if (p.compareTo(BigDecimal.ZERO) > 0 && 
                    p.compareTo(fallback.multiply(new BigDecimal("0.2"))) >= 0 &&
                    p.compareTo(fallback.multiply(new BigDecimal("4.0"))) <= 0) {
                    return p.setScale(2, RoundingMode.HALF_UP);
                }
            }
        } catch (Exception ignored) {}
        return fallback;
    }
    
    private PriceDirection parseDirection(JsonNode root, BigDecimal recommendedPrice, BigDecimal currentPrice) {
        try {
            if (root.has("direction")) {
                return PriceDirection.valueOf(root.get("direction").asText().toUpperCase());
            }
        } catch (Exception ignored) {}
        
        int cmp = recommendedPrice.compareTo(currentPrice);
        if (cmp > 0) return PriceDirection.INCREASE;
        if (cmp < 0) return PriceDirection.DECREASE;
        return PriceDirection.HOLD;
    }
    
    private Double parseConfidence(JsonNode root) {
        try {
            if (root.has("confidence")) {
                double c = root.get("confidence").asDouble();
                if (c >= 0.0 && c <= 1.0) return c;
            }
        } catch (Exception ignored) {}
        return 0.85;
    }
    
    private String parseReasoning(JsonNode root, String defaultReason) {
        try {
            if (root.has("reasoning") && !root.get("reasoning").asText().isBlank()) {
                return root.get("reasoning").asText().trim();
            }
        } catch (Exception ignored) {}
        return defaultReason;
    }
    
    private Integer parseQuantity(JsonNode root, int threshold, int currentStock) {
        try {
            if (root.has("recommendedQuantity")) {
                int q = root.get("recommendedQuantity").asInt();
                if (q > 0 && q < 50000) return q;
            }
        } catch (Exception ignored) {}
        return Math.max(1, (threshold * 3) - currentStock);
    }
    
    private Integer parseLeadTime(JsonNode root) {
        try {
            if (root.has("suggestedLeadTime")) {
                int lt = root.get("suggestedLeadTime").asInt();
                if (lt >= 1 && lt <= 90) return lt;
            }
        } catch (Exception ignored) {}
        return 7;
    }
}