package com.zycus.stockpulse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "llm")
public class LlmConfig {
    
    private String baseUrl = "http://localhost:11434/v1";
    private String apiKey = "";
    private String model = "gpt-3.5-turbo";
    private String provider = "rule-based";
    private String productHeader = "PC1";
    private String cookie = "";
    
    // Getters and setters
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProductHeader() {
        return productHeader;
    }

    public void setProductHeader(String productHeader) {
        this.productHeader = productHeader;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }
}