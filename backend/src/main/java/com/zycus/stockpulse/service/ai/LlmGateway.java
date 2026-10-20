package com.zycus.stockpulse.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zycus.stockpulse.config.LlmConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Service
public class LlmGateway {
    
    @Autowired
    private LlmConfig llmConfig;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public JsonNode callLlm(String prompt) {
        try {
            // Create RestClient instance
            RestClient restClient = RestClient.builder()
                    .baseUrl(llmConfig.getBaseUrl())
                    .build();
            
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (llmConfig.getApiKey() != null && !llmConfig.getApiKey().isEmpty() && !"your-api-key-here".equals(llmConfig.getApiKey())) {
                headers.setBearerAuth(llmConfig.getApiKey());
            }
            // Add custom headers for Zycus LiteLLM
            headers.set("product", "PC1");
            
            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", llmConfig.getModel());
            requestBody.put("messages", new Object[]{Map.of("role", "user", "content", prompt)});
            requestBody.put("temperature", 0.7);
            requestBody.put("response_format", Map.of("type", "json_object"));
            
            // Make the API call
            ResponseEntity<String> response = restClient.post()
                    .uri("/chat/completions")
                    .headers(h -> {
                        h.setContentType(MediaType.APPLICATION_JSON);
                        if (llmConfig.getApiKey() != null && !llmConfig.getApiKey().isEmpty() && !"your-api-key-here".equals(llmConfig.getApiKey())) {
                            h.setBearerAuth(llmConfig.getApiKey());
                        }
                        h.set("product", "PC1");
                    })
                    .body(requestBody)
                    .retrieve()
                    .toEntity(String.class);
            
            // Parse the response
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            return jsonResponse.get("choices").get(0).get("message").get("content");
            
        } catch (RestClientResponseException e) {
            System.err.println("LLM API error: " + e.getMessage());
            System.err.println("Response body: " + e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            System.err.println("Error calling LLM: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}