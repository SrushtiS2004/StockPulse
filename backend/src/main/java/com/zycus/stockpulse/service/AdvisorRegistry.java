package com.zycus.stockpulse.service;

import com.zycus.stockpulse.config.LlmConfig;
import com.zycus.stockpulse.entity.Product;
import com.zycus.stockpulse.entity.PricingSuggestion;
import com.zycus.stockpulse.entity.ReorderSuggestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdvisorRegistry {
    
    @Autowired
    private RuleBasedCommerceAdvisor ruleBasedAdvisor;
    
    @Autowired
    private AICommerceAdvisor aiAdvisor;
    
    @Autowired
    private LlmConfig llmConfig;
    
    private final Map<String, CommerceAdvisor> advisors = new ConcurrentHashMap<>();
    private volatile CommerceAdvisor activeAdvisor;
    
    @PostConstruct
    public void init() {
        // Register available advisors
        advisors.put("rule-based", ruleBasedAdvisor);
        advisors.put("ai", aiAdvisor);
        
        // Set initial active advisor
        setActiveAdvisor("rule-based");
    }
    
    public void setActiveAdvisor(String strategy) {
        CommerceAdvisor advisor = advisors.get(strategy);
        if (advisor != null) {
            this.activeAdvisor = advisor;
        } else {
            this.activeAdvisor = ruleBasedAdvisor; // fallback
        }
    }
    
    public CommerceAdvisor getActiveAdvisor() {
        // Check configuration for strategy change
        String provider = llmConfig.getProvider();
        if (!provider.equals(getCurrentStrategy())) {
            setActiveAdvisor(provider);
        }
        return activeAdvisor;
    }
    
    public String getCurrentStrategy() {
        if (activeAdvisor == ruleBasedAdvisor) {
            return "rule-based";
        } else if (activeAdvisor == aiAdvisor) {
            return "ai";
        }
        return "rule-based";
    }
    
    public PricingSuggestion generatePricingSuggestion(Product product) {
        return getActiveAdvisor().generatePricingSuggestion(product);
    }
    
    public ReorderSuggestion generateReorderSuggestion(Product product) {
        return getActiveAdvisor().generateReorderSuggestion(product);
    }
}