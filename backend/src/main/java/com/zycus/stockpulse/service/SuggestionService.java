package com.zycus.stockpulse.service;

import com.zycus.stockpulse.entity.*;
import com.zycus.stockpulse.repository.PricingSuggestionRepository;
import com.zycus.stockpulse.repository.ProductRepository;
import com.zycus.stockpulse.repository.ReorderSuggestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SuggestionService {
    
    private static final Logger log = LoggerFactory.getLogger(SuggestionService.class);
    
    @Autowired
    private PricingSuggestionRepository pricingSuggestionRepository;
    
    @Autowired
    private ReorderSuggestionRepository reorderSuggestionRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private AdvisorRegistry advisorRegistry;
    
    @Transactional
    public PricingSuggestion createPricingSuggestion(Long productId, TriggerReason triggerReason) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return null;
        }
        Product product = productOpt.get();
        
        // Idempotency: Skip duplicate PENDING suggestion for same product + triggerReason
        Optional<PricingSuggestion> existing = pricingSuggestionRepository
                .findByProductAndStatusAndTriggerReason(product, SuggestionStatus.PENDING, triggerReason);
        if (existing.isPresent()) {
            log.info("Skipping duplicate pending pricing suggestion for SKU: {} with trigger: {}", 
                     product.getSku(), triggerReason);
            return existing.get();
        }
        
        PricingSuggestion suggestion = advisorRegistry.generatePricingSuggestion(product, triggerReason);
        PricingSuggestion saved = pricingSuggestionRepository.save(suggestion);
        
        // Transition product lifecycle to PRICE_REVIEW_PENDING
        if (product.getStatus() == ProductStatus.ACTIVE) {
            product.setStatus(ProductStatus.PRICE_REVIEW_PENDING);
            productRepository.save(product);
        }
        
        log.info("Generated PricingSuggestion #{} for SKU {}: {} ${} -> ${} ({})",
                 saved.getId(), product.getSku(), saved.getDirection(), 
                 saved.getCurrentPrice(), saved.getRecommendedPrice(), saved.getTriggerReason());
        
        return saved;
    }
    
    @Transactional
    public ReorderSuggestion createReorderSuggestion(Long productId, TriggerReason triggerReason) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return null;
        }
        Product product = productOpt.get();
        
        // Idempotency: Skip duplicate PENDING suggestion for same product + triggerReason
        Optional<ReorderSuggestion> existing = reorderSuggestionRepository
                .findByProductAndStatusAndTriggerReason(product, SuggestionStatus.PENDING, triggerReason);
        if (existing.isPresent()) {
            log.info("Skipping duplicate pending reorder suggestion for SKU: {} with trigger: {}", 
                     product.getSku(), triggerReason);
            return existing.get();
        }
        
        ReorderSuggestion suggestion = advisorRegistry.generateReorderSuggestion(product, triggerReason);
        ReorderSuggestion saved = reorderSuggestionRepository.save(suggestion);
        
        log.info("Generated ReorderSuggestion #{} for SKU {}: qty {} units (lead time {} days)",
                 saved.getId(), product.getSku(), saved.getRecommendedQuantity(), saved.getSuggestedLeadTime());
        
        return saved;
    }
    
    public List<PricingSuggestion> getPricingSuggestionsByStatus(SuggestionStatus status) {
        return pricingSuggestionRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    public List<ReorderSuggestion> getReorderSuggestionsByStatus(SuggestionStatus status) {
        return reorderSuggestionRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    public Optional<PricingSuggestion> getPricingSuggestionById(Long id) {
        return pricingSuggestionRepository.findById(id);
    }
    
    public Optional<ReorderSuggestion> getReorderSuggestionById(Long id) {
        return reorderSuggestionRepository.findById(id);
    }
    
    @Transactional
    public PricingSuggestion updatePricingSuggestionStatus(Long id, SuggestionStatus newStatus) {
        Optional<PricingSuggestion> opt = pricingSuggestionRepository.findById(id);
        if (opt.isEmpty()) {
            return null;
        }
        
        PricingSuggestion suggestion = opt.get();
        SuggestionStatus oldStatus = suggestion.getStatus();
        suggestion.setStatus(newStatus);
        PricingSuggestion savedSuggestion = pricingSuggestionRepository.save(suggestion);
        
        // If ACCEPTED: Atomically update Product.currentPrice
        if (newStatus == SuggestionStatus.ACCEPTED && oldStatus != SuggestionStatus.ACCEPTED) {
            Product product = suggestion.getProduct();
            product.applyNewPrice(suggestion.getRecommendedPrice());
            productRepository.save(product);
            log.info("Accepted pricing suggestion #{}: SKU {} price updated to ${}",
                     id, product.getSku(), product.getCurrentPrice());
        }
        
        // Check if any other pricing suggestions remain pending for this product
        Product product = suggestion.getProduct();
        long pendingCount = pricingSuggestionRepository.countByProductAndStatus(product, SuggestionStatus.PENDING);
        if (pendingCount == 0 && product.getStatus() == ProductStatus.PRICE_REVIEW_PENDING) {
            if (product.getStockLevel() > product.getReorderThreshold()) {
                product.setStatus(ProductStatus.ACTIVE);
                productRepository.save(product);
            }
        }
        
        return savedSuggestion;
    }
    
    @Transactional
    public ReorderSuggestion updateReorderSuggestionStatus(Long id, SuggestionStatus newStatus) {
        Optional<ReorderSuggestion> opt = reorderSuggestionRepository.findById(id);
        if (opt.isEmpty()) {
            return null;
        }
        
        ReorderSuggestion suggestion = opt.get();
        SuggestionStatus oldStatus = suggestion.getStatus();
        suggestion.setStatus(newStatus);
        ReorderSuggestion savedSuggestion = reorderSuggestionRepository.save(suggestion);
        
        // If ACCEPTED: Simulate inbound shipment by incrementing product.stockLevel
        if (newStatus == SuggestionStatus.ACCEPTED && oldStatus != SuggestionStatus.ACCEPTED) {
            Product product = suggestion.getProduct();
            product.replenishStock(suggestion.getRecommendedQuantity());
            productRepository.save(product);
            log.info("Accepted reorder suggestion #{}: SKU {} stock replenished by {} units to new stock {}",
                     id, product.getSku(), suggestion.getRecommendedQuantity(), product.getStockLevel());
        }
        
        return savedSuggestion;
    }
}