package com.zycus.stockpulse.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class AdvisorRegistryTest {

    @Test
    @DisplayName("Should initialize advisors and switch strategy dynamically")
    void testStrategySwitching() {
        AdvisorRegistry registry = new AdvisorRegistry();
        RuleBasedCommerceAdvisor ruleAdvisor = new RuleBasedCommerceAdvisor();
        
        ReflectionTestUtils.setField(registry, "ruleBasedAdvisor", ruleAdvisor);
        ReflectionTestUtils.setField(registry, "aiAdvisor", null);
        ReflectionTestUtils.setField(registry, "defaultStrategy", "rule-based");

        registry.init();

        CommerceAdvisor active = registry.getActiveAdvisor();
        assertNotNull(active);
        assertEquals("rule-based", active.getStrategyName());
        assertEquals("rule-based", registry.getCurrentStrategy());

        // Test switching to rule-based explicitly
        registry.setActiveAdvisor("rule-based");
        assertEquals("rule-based", registry.getCurrentStrategy());
    }
}
