package com.zycus.stockpulse.service;

import com.zycus.stockpulse.entity.Category;
import com.zycus.stockpulse.entity.PricingSuggestion;
import com.zycus.stockpulse.entity.Product;
import com.zycus.stockpulse.entity.ReorderSuggestion;
import com.zycus.stockpulse.entity.TriggerReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RuleBasedCommerceAdvisorTest {

    private RuleBasedCommerceAdvisor advisor;

    @BeforeEach
    void setUp() {
        advisor = new RuleBasedCommerceAdvisor();
    }

    @Test
    @DisplayName("Should maintain strategy name as rule-based")
    void testGetStrategyName() {
        assertEquals("rule-based", advisor.getStrategyName());
    }

    @Test
    @DisplayName("Should generate reorder proposal when stock is below threshold")
    void testSuggestReorderLowStock() {
        Product product = new Product("PRD-TEST-1", "Test Product", Category.ELECTRONICS, 
                BigDecimal.valueOf(100.0), 5, 20, 4);
        product.setId(1L);

        ReorderSuggestion suggestion = advisor.generateReorderSuggestion(product, TriggerReason.INVENTORY_LOW);

        assertNotNull(suggestion);
        assertEquals("PRD-TEST-1", suggestion.getProduct().getSku());
        assertEquals(TriggerReason.INVENTORY_LOW, suggestion.getTriggerReason());
        assertTrue(suggestion.getRecommendedQuantity() > 0);
        assertTrue(suggestion.getSuggestedLeadTime() > 0);
        assertTrue(suggestion.getConfidence() >= 0.8);
        assertTrue(suggestion.getReasoning().contains("breached reorder threshold"));
    }

    @Test
    @DisplayName("Should recommend price surge on demand spike")
    void testSuggestPricingDemandSpike() {
        Product product = new Product("PRD-SPIKE", "High Velocity Item", Category.APPAREL, 
                BigDecimal.valueOf(50.0), 30, 10, 25);
        product.setId(2L);

        PricingSuggestion suggestion = advisor.generatePricingSuggestion(product, TriggerReason.DEMAND_SPIKE);

        assertNotNull(suggestion);
        assertEquals("PRD-SPIKE", suggestion.getProduct().getSku());
        assertEquals(TriggerReason.DEMAND_SPIKE, suggestion.getTriggerReason());
        assertTrue(suggestion.getRecommendedPrice().compareTo(product.getCurrentPrice()) >= 0, 
                "Surge price should be higher than or equal to current price");
        assertTrue(suggestion.getConfidence() >= 0.8);
    }

    @Test
    @DisplayName("Should recommend price markdown on stagnant slow inventory")
    void testSuggestPricingStagnantInventory() {
        Product product = new Product("PRD-SLOW", "Slow Moving Shoes", Category.APPAREL, 
                BigDecimal.valueOf(80.0), 100, 20, 0);
        product.setId(3L);

        PricingSuggestion suggestion = advisor.generatePricingSuggestion(product, TriggerReason.MANUAL);

        assertNotNull(suggestion);
        assertTrue(suggestion.getRecommendedPrice().compareTo(product.getCurrentPrice()) <= 0, 
                "Slow moving inventory should be marked down or held");
    }
}
