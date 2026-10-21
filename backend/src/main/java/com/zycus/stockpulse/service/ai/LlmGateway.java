package com.zycus.stockpulse.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zycus.stockpulse.config.LlmConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class LlmGateway {
    
    private static final Logger log = LoggerFactory.getLogger(LlmGateway.class);
    
    @Autowired
    private LlmConfig llmConfig;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String callLlmRaw(String prompt) {
        try {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(3500);
            requestFactory.setReadTimeout(6000);

            RestClient restClient = RestClient.builder()
                    .baseUrl(llmConfig.getBaseUrl())
                    .requestFactory(requestFactory)
                    .build();

            // Prepare request body (OpenAI-compatible)
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", llmConfig.getModel());
            requestBody.put("messages", new Object[]{
                Map.of("role", "system", "content", "You are an expert commerce merchandising AI advisor. Return ONLY valid JSON."),
                Map.of("role", "user", "content", prompt)
            });
            requestBody.put("temperature", 0.4);
            requestBody.put("response_format", Map.of("type", "json_object"));

            ResponseEntity<String> response = restClient.post()
                    .uri("/chat/completions")
                    .headers(h -> {
                        h.setContentType(MediaType.APPLICATION_JSON);
                        if (llmConfig.getApiKey() != null && !llmConfig.getApiKey().isBlank()) {
                            h.setBearerAuth(llmConfig.getApiKey());
                        }
                        // Zycus LiteLLM headers
                        if (llmConfig.getProductHeader() != null && !llmConfig.getProductHeader().isBlank()) {
                            h.set("product", llmConfig.getProductHeader());
                        }
                        if (llmConfig.getCookie() != null && !llmConfig.getCookie().isBlank()) {
                            h.set("Cookie", llmConfig.getCookie());
                        }
                    })
                    .body(requestBody)
                    .retrieve()
                    .toEntity(String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                JsonNode choices = json.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    JsonNode message = choices.get(0).get("message");
                    if (message != null && message.has("content")) {
                        return message.get("content").asText();
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("LLM API call failed or timed out: {}. Using rule-based fallback.", e.getMessage());
            return null;
        }
    }
}