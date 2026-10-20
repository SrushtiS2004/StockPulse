package com.zycus.stockpulse.service;

import com.zycus.stockpulse.entity.*;
import com.zycus.stockpulse.repository.PricingSuggestionRepository;
import com.zycus.stockpulse.repository.ReorderSuggestionRepository;
import com.zycus.stockpulse.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SuggestionService {
    
    @Autowired
    private PricingSuggestionRepository pricingSuggestionRepository;
    
    @Autowired
    private ReorderSuggestionRepository reorderSuggestionRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private AdvisorRegistry advisorRegistry;
    
    public PricingSuggestion createPricingSuggestion(Long productId, TriggerReason triggerReason) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            
            // Check if there's already a pending suggestion for this product and trigger reason
            Optional<PricingSuggestion> existingSuggestion = 
                pricingSuggestionRepository.findPendingByProductAndTriggerReason(product, triggerReason);
            
            if (existingSuggestion.isPresent()) {
                return existingSuggestion.get();
            }
            
            // Generate suggestion using the active advisor from registry
            PricingSuggestion suggestion = advisorRegistry.generatePricingSuggestion(product);
            suggestion.setTriggerReason(triggerReason);
            return pricingSuggestionRepository.save(suggestion);
        }
        return null;
    }
    
    public ReorderSuggestion createReorderSuggestion(Long productId, TriggerReason triggerReason) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            
            // Check if there's already a pending suggestion for this product and trigger reason
            Optional<ReorderSuggestion> existingSuggestion = 
                reorderSuggestionRepository.findPendingByProductAndTriggerReason(product, triggerReason);
            
            if (existingSuggestion.isPresent()) {
                return existingSuggestion.get();
            }
            
            // Generate suggestion using the active advisor from registry
            ReorderSuggestion suggestion = advisorRegistry.generateReorderSuggestion(product);
            suggestion.setTriggerReason(triggerReason);
            return reorderSuggestionRepository.save(suggestion);
        }
        return null;
    }
    
    public List<PricingSuggestion> getPricingSuggestionsByStatus(SuggestionStatus status) {
        return pricingSuggestionRepository.findByStatus(status);
    }
    
    public List<ReorderSuggestion> getReorderSuggestionsByStatus(SuggestionStatus status) {
        return reorderSuggestionRepository.findByStatus(status);
    }
    
    public Optional<PricingSuggestion> getPricingSuggestionById(Long id) {
        return pricingSuggestionRepository.findById(id);
    }
    
    public Optional<ReorderSuggestion> getReorderSuggestionById(Long id) {
        return reorderSuggestionRepository.findById(id);
    }
    
    public PricingSuggestion updatePricingSuggestionStatus(Long id, SuggestionStatus status) {
        Optional<PricingSuggestion> suggestionOpt = pricingSuggestionRepository.findById(id);
        if (suggestionOpt.isPresent()) {
            PricingSuggestion suggestion = suggestionOpt.get();
            SuggestionStatus oldStatus = suggestion.getStatus();
            suggestion.setStatus(status);
            PricingSuggestion updatedSuggestion = pricingSuggestionRepository.save(suggestion);
            
            // If accepted, update the product price
            if (status == SuggestionStatus.ACCEPTED && oldStatus != SuggestionStatus.ACCEPTED) {
                Product product = suggestion.getProduct();
                product.setCurrentPrice(suggestion.getRecommendedPrice());
                productRepository.save(product);
            }
            
            return updatedSuggestion;
        }
        return null;
    }
    
    public ReorderSuggestion updateReorderSuggestionStatus(Long id, SuggestionStatus status) {
        Optional<ReorderSuggestion> suggestionOpt = reorderSuggestionRepository.findById(id);
        if (suggestionOpt.isPresent()) {
            ReorderSuggestion suggestion = suggestionOpt.get();
            SuggestionStatus oldStatus = suggestion.getStatus();
            suggestion.setStatus(status);
            ReorderSuggestion updatedSuggestion = reorderSuggestionRepository.save(suggestion);
            
            // If accepted, update the product stock
            if (status == SuggestionStatus.ACCEPTED && oldStatus != SuggestionStatus.ACCEPTED) {
                Product product = suggestion.getProduct();
                int newStock = product.getStockLevel() + suggestion.getRecommendedQuantity();
                product.setStockLevel(newStock);
                productRepository.save(product);
            }
            
            return updatedSuggestion;
        }
        return null;
    }
}