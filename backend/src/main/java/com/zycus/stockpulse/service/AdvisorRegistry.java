package com.zycus.stockpulse.service;

import com.zycus.stockpulse.entity.PricingSuggestion;
import com.zycus.stockpulse.entity.Product;
import com.zycus.stockpulse.entity.ReorderSuggestion;
import com.zycus.stockpulse.entity.TriggerReason;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdvisorRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(AdvisorRegistry.class);
    
    @Autowired
    private RuleBasedCommerceAdvisor ruleBasedAdvisor;
    
    @Autowired
    private AICommerceAdvisor aiAdvisor;
    
    @Value("${commerce.strategy:ai}")
    private String defaultStrategy;
    
    private final Map<String, CommerceAdvisor> advisors = new ConcurrentHashMap<>();
    private volatile CommerceAdvisor activeAdvisor;
    
    @PostConstruct
    public void init() {
        if (ruleBasedAdvisor != null) {
            advisors.put("rule-based", ruleBasedAdvisor);
        }
        if (aiAdvisor != null) {
            advisors.put("ai", aiAdvisor);
        }
        
        setActiveAdvisor(defaultStrategy != null ? defaultStrategy : "rule-based");
        log.info("StockPulse AdvisorRegistry initialized with active strategy: {}", getCurrentStrategy());
    }
    
    public void setActiveAdvisor(String strategy) {
        String key = strategy.toLowerCase().trim();
        if (advisors.containsKey(key)) {
            this.activeAdvisor = advisors.get(key);
            log.info("Commerce advisor switched to: {}", key);
        } else {
            log.warn("Unknown strategy '{}', defaulting to AI advisor", strategy);
            this.activeAdvisor = aiAdvisor;
        }
    }
    
    public CommerceAdvisor getActiveAdvisor() {
        if (activeAdvisor == null) {
            activeAdvisor = aiAdvisor;
        }
        return activeAdvisor;
    }
    
    public String getCurrentStrategy() {
        return getActiveAdvisor().getStrategyName();
    }
    
    public Set<String> getAvailableStrategies() {
        return advisors.keySet();
    }
    
    public PricingSuggestion generatePricingSuggestion(Product product, TriggerReason triggerReason) {
        return getActiveAdvisor().generatePricingSuggestion(product, triggerReason);
    }
    
    public ReorderSuggestion generateReorderSuggestion(Product product, TriggerReason triggerReason) {
        return getActiveAdvisor().generateReorderSuggestion(product, triggerReason);
    }
}