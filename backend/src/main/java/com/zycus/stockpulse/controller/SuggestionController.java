package com.zycus.stockpulse.controller;

import com.zycus.stockpulse.dto.PricingSuggestionDTO;
import com.zycus.stockpulse.dto.ReorderSuggestionDTO;
import com.zycus.stockpulse.dto.UpdateSuggestionRequest;
import com.zycus.stockpulse.entity.*;
import com.zycus.stockpulse.service.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SuggestionController {
    
    @Autowired
    private SuggestionService suggestionService;
    
    @PostMapping("/products/{id}/suggest-pricing")
    public ResponseEntity<PricingSuggestionDTO> createPricingSuggestion(
            @PathVariable Long id,
            @RequestParam(defaultValue = "MANUAL") TriggerReason triggerReason) {
        
        PricingSuggestion suggestion = suggestionService.createPricingSuggestion(id, triggerReason);
        if (suggestion != null) {
            return ResponseEntity.ok(convertToDTO(suggestion));
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/products/{id}/suggest-reorder")
    public ResponseEntity<ReorderSuggestionDTO> createReorderSuggestion(
            @PathVariable Long id,
            @RequestParam(defaultValue = "MANUAL") TriggerReason triggerReason) {
        
        ReorderSuggestion suggestion = suggestionService.createReorderSuggestion(id, triggerReason);
        if (suggestion != null) {
            return ResponseEntity.ok(convertToDTO(suggestion));
        }
        return ResponseEntity.notFound().build();
    }
    
    @PatchMapping("/pricing-suggestions/{id}")
    public ResponseEntity<PricingSuggestionDTO> updatePricingSuggestion(
            @PathVariable Long id,
            @RequestBody UpdateSuggestionRequest request) {
        
        PricingSuggestion updatedSuggestion = suggestionService.updatePricingSuggestionStatus(id, request.getStatus());
        if (updatedSuggestion != null) {
            return ResponseEntity.ok(convertToDTO(updatedSuggestion));
        }
        return ResponseEntity.notFound().build();
    }
    
    @PatchMapping("/reorder-suggestions/{id}")
    public ResponseEntity<ReorderSuggestionDTO> updateReorderSuggestion(
            @PathVariable Long id,
            @RequestBody UpdateSuggestionRequest request) {
        
        ReorderSuggestion updatedSuggestion = suggestionService.updateReorderSuggestionStatus(id, request.getStatus());
        if (updatedSuggestion != null) {
            return ResponseEntity.ok(convertToDTO(updatedSuggestion));
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/pricing-suggestions")
    public ResponseEntity<List<PricingSuggestionDTO>> getPricingSuggestions(
            @RequestParam(defaultValue = "PENDING") SuggestionStatus status) {
        
        List<PricingSuggestion> suggestions = suggestionService.getPricingSuggestionsByStatus(status);
        List<PricingSuggestionDTO> suggestionDTOs = suggestions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(suggestionDTOs);
    }
    
    @GetMapping("/reorder-suggestions")
    public ResponseEntity<List<ReorderSuggestionDTO>> getReorderSuggestions(
            @RequestParam(defaultValue = "PENDING") SuggestionStatus status) {
        
        List<ReorderSuggestion> suggestions = suggestionService.getReorderSuggestionsByStatus(status);
        List<ReorderSuggestionDTO> suggestionDTOs = suggestions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(suggestionDTOs);
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