package com.zycus.stockpulse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zycus.stockpulse.entity.*;
import com.zycus.stockpulse.repository.ProductRepository;
import com.zycus.stockpulse.service.ai.LlmGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class AICommerceAdvisor implements CommerceAdvisor {
    
    @Autowired
    private LlmGateway llmGateway;
    
    @Autowired
    private ProductRepository productRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public PricingSuggestion generatePricingSuggestion(Product product) {
        // Try to get AI recommendation
        try {
            String prompt = createPricingPrompt(product);
            JsonNode aiResponse = llmGateway.callLlm(prompt);
            
            if (aiResponse != null && !aiResponse.asText().isEmpty()) {
                JsonNode parsedResponse = objectMapper.readTree(aiResponse.asText());
                
                // Extract values with validation
                BigDecimal recommendedPrice = parseBigDecimal(parsedResponse, "recommendedPrice", product.getCurrentPrice());
                PriceDirection direction = parsePriceDirection(parsedResponse, "direction");
                Double confidence = parseDouble(parsedResponse, "pricingConfidence", 0.8);
                String reasoning = parseString(parsedResponse, "pricingReasoning", "AI-generated pricing recommendation");
                
                return new PricingSuggestion(
                    product,
                    product.getCurrentPrice(),
                    recommendedPrice,
                    direction,
                    confidence,
                    reasoning,
                    TriggerReason.MANUAL
                );
            }
        } catch (Exception e) {
            System.err.println("Error processing AI pricing suggestion: " + e.getMessage());
        }
        
        // Fallback to rule-based
        return new RuleBasedCommerceAdvisor().generatePricingSuggestion(product);
    }
    
    @Override
    public ReorderSuggestion generateReorderSuggestion(Product product) {
        // Try to get AI recommendation
        try {
            String prompt = createReorderPrompt(product);
            JsonNode aiResponse = llmGateway.callLlm(prompt);
            
            if (aiResponse != null && !aiResponse.asText().isEmpty()) {
                JsonNode parsedResponse = objectMapper.readTree(aiResponse.asText());
                
                // Extract values with validation
                Integer recommendedQuantity = parseInteger(parsedResponse, "recommendedQuantity", 10);
                Double confidence = parseDouble(parsedResponse, "reorderConfidence", 0.8);
                String reasoning = parseString(parsedResponse, "reorderReasoning", "AI-generated reorder recommendation");
                Integer leadTime = parseInteger(parsedResponse, "suggestedLeadTime", 7);
                
                return new ReorderSuggestion(
                    product,
                    product.getStockLevel(),
                    recommendedQuantity,
                    leadTime,
                    confidence,
                    reasoning,
                    TriggerReason.MANUAL
                );
            }
        } catch (Exception e) {
            System.err.println("Error processing AI reorder suggestion: " + e.getMessage());
        }
        
        // Fallback to rule-based
        return new RuleBasedCommerceAdvisor().generateReorderSuggestion(product);
    }
    private String createPricingPrompt(Product product) {
        Double categoryAvgVelocity = productRepository.findAverageDemandVelocityByCategory(product.getCategory());
        if (categoryAvgVelocity == null) categoryAvgVelocity = 0.0;
        
        return String.format("""
            You are an AI commerce advisor for an online retail store. Analyze the following product and provide pricing recommendations.
            
            Product Information:
            - SKU: %s
            - Name: %s
            - Category: %s
            - Current Price: $%.2f
            - Stock Level: %d
            - Reorder Threshold: %d
            - Demand Velocity: %d orders/day
            - Category Average Velocity: %.1f orders/day
            
            Based on this information, provide a JSON response with the following structure:
            {
                "recommendedPrice": number,
                "direction": "INCREASE|DECREASE|HOLD",
                "pricingConfidence": number (0-1),
                "pricingReasoning": "plain English explanation"
            }
            
            Consider factors like stock scarcity, demand trends, competitive positioning, and profit margins.
            Keep price changes reasonable (typically 5-15%%).
            """, 
            product.getSku(),
            product.getName(),
            product.getCategory(),
            product.getCurrentPrice(),
            product.getStockLevel(),
            product.getReorderThreshold(),
            product.getDemandVelocity(),
            categoryAvgVelocity
        );
    }
    
    private String createReorderPrompt(Product product) {
        return String.format("""
            You are an AI commerce advisor for an online retail store. Analyze the following product and provide reorder recommendations.
            
            Product Information:
            - SKU: %s
            - Name: %s
            - Category: %s
            - Current Stock: %d
            - Reorder Threshold: %d
            - Demand Velocity: %d orders/day
            
            Based on this information, provide a JSON response with the following structure:
            {
                "recommendedQuantity": integer,
                "reorderConfidence": number (0-1),
                "reorderReasoning": "plain English explanation",
                "suggestedLeadTime": integer (days)
            }
            
            Consider factors like current stock, demand rate, seasonal trends, supplier reliability, and storage costs.
            Ensure recommended quantities are realistic and economically sensible.
            """, 
            product.getSku(),
            product.getName(),
            product.getCategory(),
            product.getStockLevel(),
            product.getReorderThreshold(),
            product.getDemandVelocity()
        );
    }
    
    // Helper methods for parsing with validation
    private BigDecimal parseBigDecimal(JsonNode node, String field, BigDecimal defaultValue) {
        try {
            JsonNode valueNode = node.get(field);
            if (valueNode != null && !valueNode.isNull()) {
                BigDecimal value = new BigDecimal(valueNode.asText());
                if (value.compareTo(BigDecimal.ZERO) > 0 && value.compareTo(new BigDecimal("10000")) < 0) {
                    return value.setScale(2, RoundingMode.HALF_UP);
                }
            }
        } catch (Exception e) {
            // Fall through to default
        }
        return defaultValue;
    }
    
    private PriceDirection parsePriceDirection(JsonNode node, String field) {
        try {
            JsonNode valueNode = node.get(field);
            if (valueNode != null && !valueNode.isNull()) {
                return PriceDirection.valueOf(valueNode.asText().toUpperCase());
            }
        } catch (Exception e) {
            // Fall through to default
        }
        return PriceDirection.HOLD;
    }
    
    private Double parseDouble(JsonNode node, String field, Double defaultValue) {
        try {
            JsonNode valueNode = node.get(field);
            if (valueNode != null && !valueNode.isNull()) {
                Double value = valueNode.asDouble();
                if (value >= 0.0 && value <= 1.0) {
                    return value;
                }
            }
        } catch (Exception e) {
            // Fall through to default
        }
        return defaultValue;
    }
    
    private String parseString(JsonNode node, String field, String defaultValue) {
        try {
            JsonNode valueNode = node.get(field);
            if (valueNode != null && !valueNode.isNull()) {
                String value = valueNode.asText();
                if (value != null && !value.trim().isEmpty()) {
                    return value.trim();
                }
            }
        } catch (Exception e) {
            // Fall through to default
        }
        return defaultValue;
    }
    
    private Integer parseInteger(JsonNode node, String field, Integer defaultValue) {
        try {
            JsonNode valueNode = node.get(field);
            if (valueNode != null && !valueNode.isNull()) {
                Integer value = valueNode.asInt();
                if (value > 0 && value < 10000) {
                    return value;
                }
            }
        } catch (Exception e) {
            // Fall through to default
        }
        return defaultValue;
    }
}
}