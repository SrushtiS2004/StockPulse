package com.zycus.stockpulse.controller;

import com.zycus.stockpulse.dto.PricingSuggestionDTO;
import com.zycus.stockpulse.dto.ReorderSuggestionDTO;
import com.zycus.stockpulse.dto.UpdateSuggestionRequest;
import com.zycus.stockpulse.entity.*;
import com.zycus.stockpulse.service.AdvisorRegistry;
import com.zycus.stockpulse.service.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
public class SuggestionController {
    
    @Autowired
    private SuggestionService suggestionService;
    
    @Autowired
    private AdvisorRegistry advisorRegistry;
    
    // On-demand pricing suggestion endpoints
    @PostMapping({"/api/products/{id}/suggest-pricing", "/products/{id}/suggest-pricing"})
    public ResponseEntity<PricingSuggestionDTO> createPricingSuggestion(
            @PathVariable Long id,
            @RequestParam(defaultValue = "MANUAL") TriggerReason triggerReason) {
        
        PricingSuggestion suggestion = suggestionService.createPricingSuggestion(id, triggerReason);
        if (suggestion != null) {
            return ResponseEntity.ok(convertToDTO(suggestion));
        }
        return ResponseEntity.notFound().build();
    }
    
    // On-demand reorder suggestion endpoints
    @PostMapping({"/api/products/{id}/suggest-reorder", "/products/{id}/suggest-reorder"})
    public ResponseEntity<ReorderSuggestionDTO> createReorderSuggestion(
            @PathVariable Long id,
            @RequestParam(defaultValue = "MANUAL") TriggerReason triggerReason) {
        
        ReorderSuggestion suggestion = suggestionService.createReorderSuggestion(id, triggerReason);
        if (suggestion != null) {
            return ResponseEntity.ok(convertToDTO(suggestion));
        }
        return ResponseEntity.notFound().build();
    }
    
    // Suggestion review & decision endpoints
    @PatchMapping({"/api/pricing-suggestions/{id}", "/pricing-suggestions/{id}"})
    public ResponseEntity<PricingSuggestionDTO> updatePricingSuggestion(
            @PathVariable Long id,
            @RequestBody UpdateSuggestionRequest request) {
        
        PricingSuggestion updated = suggestionService.updatePricingSuggestionStatus(id, request.getStatus());
        if (updated != null) {
            return ResponseEntity.ok(convertToDTO(updated));
        }
        return ResponseEntity.notFound().build();
    }
    
    @PatchMapping({"/api/reorder-suggestions/{id}", "/reorder-suggestions/{id}"})
    public ResponseEntity<ReorderSuggestionDTO> updateReorderSuggestion(
            @PathVariable Long id,
            @RequestBody UpdateSuggestionRequest request) {
        
        ReorderSuggestion updated = suggestionService.updateReorderSuggestionStatus(id, request.getStatus());
        if (updated != null) {
            return ResponseEntity.ok(convertToDTO(updated));
        }
        return ResponseEntity.notFound().build();
    }
    
    // List suggestions
    @GetMapping({"/api/pricing-suggestions", "/pricing-suggestions"})
    public ResponseEntity<List<PricingSuggestionDTO>> getPricingSuggestions(
            @RequestParam(defaultValue = "PENDING") SuggestionStatus status) {
        
        List<PricingSuggestion> suggestions = suggestionService.getPricingSuggestionsByStatus(status);
        List<PricingSuggestionDTO> dtos = suggestions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping({"/api/reorder-suggestions", "/reorder-suggestions"})
    public ResponseEntity<List<ReorderSuggestionDTO>> getReorderSuggestions(
            @RequestParam(defaultValue = "PENDING") SuggestionStatus status) {
        
        List<ReorderSuggestion> suggestions = suggestionService.getReorderSuggestionsByStatus(status);
        List<ReorderSuggestionDTO> dtos = suggestions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    // Strategy runtime switching endpoints
    @GetMapping({"/api/commerce/strategy", "/commerce/strategy"})
    public ResponseEntity<Map<String, Object>> getCommerceStrategy() {
        return ResponseEntity.ok(Map.of(
            "activeStrategy", advisorRegistry.getCurrentStrategy(),
            "availableStrategies", advisorRegistry.getAvailableStrategies()
        ));
    }
    
    @PostMapping({"/api/commerce/strategy", "/commerce/strategy"})
    public ResponseEntity<Map<String, Object>> setCommerceStrategy(@RequestParam String strategy) {
        advisorRegistry.setActiveAdvisor(strategy);
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "activeStrategy", advisorRegistry.getCurrentStrategy()
        ));
    }
    
    private PricingSuggestionDTO convertToDTO(PricingSuggestion suggestion) {
        PricingSuggestionDTO dto = new PricingSuggestionDTO();
        dto.setId(suggestion.getId());
        dto.setProductId(suggestion.getProduct().getId());
        dto.setProductName(suggestion.getProduct().getName());
        dto.setProductSku(suggestion.getProduct().getSku());
        dto.setCurrentPrice(suggestion.getCurrentPrice());
        dto.setRecommendedPrice(suggestion.getRecommendedPrice());
        dto.setDirection(suggestion.getDirection());
        dto.setConfidence(suggestion.getConfidence());
        dto.setReasoning(suggestion.getReasoning());
        dto.setStatus(suggestion.getStatus());
        dto.setTriggerReason(suggestion.getTriggerReason());
        return dto;
    }
    
    private ReorderSuggestionDTO convertToDTO(ReorderSuggestion suggestion) {
        ReorderSuggestionDTO dto = new ReorderSuggestionDTO();
        dto.setId(suggestion.getId());
        dto.setProductId(suggestion.getProduct().getId());
        dto.setProductName(suggestion.getProduct().getName());
        dto.setProductSku(suggestion.getProduct().getSku());
        dto.setCurrentStock(suggestion.getCurrentStock());
        dto.setRecommendedQuantity(suggestion.getRecommendedQuantity());
        dto.setSuggestedLeadTime(suggestion.getSuggestedLeadTime());
        dto.setConfidence(suggestion.getConfidence());
        dto.setReasoning(suggestion.getReasoning());
        dto.setStatus(suggestion.getStatus());
        dto.setTriggerReason(suggestion.getTriggerReason());
        return dto;
    }
}