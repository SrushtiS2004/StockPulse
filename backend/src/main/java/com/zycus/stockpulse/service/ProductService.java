package com.zycus.stockpulse.service;

import com.zycus.stockpulse.entity.Category;
import com.zycus.stockpulse.entity.Product;
import com.zycus.stockpulse.entity.ProductStatus;
import com.zycus.stockpulse.entity.TriggerReason;
import com.zycus.stockpulse.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductEventPublisher eventPublisher;
    
    @Autowired
    private SuggestionService suggestionService;

    @Autowired
    private com.zycus.stockpulse.repository.PricingSuggestionRepository pricingSuggestionRepository;

    @Autowired
    private com.zycus.stockpulse.repository.ReorderSuggestionRepository reorderSuggestionRepository;
    
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public List<Product> getProductsByStatus(ProductStatus status) {
        return productRepository.findByStatus(status);
    }
    
    public List<Product> getProductsByCategory(Category category) {
        return productRepository.findByCategory(category);
    }
    
    public List<Product> getProductsByStatusAndCategory(ProductStatus status, Category category) {
        return productRepository.findByStatusAndCategory(status, category);
    }
    
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    
    public Optional<Product> getProductByIdOrSku(String identifier) {
        try {
            Long id = Long.parseLong(identifier);
            Optional<Product> p = productRepository.findById(id);
            if (p.isPresent()) return p;
        } catch (NumberFormatException ignored) {}
        return productRepository.findBySku(identifier);
    }
    
    @Transactional
    public Product recordSale(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            return null;
        }
        
        Product product = productOpt.get();
        product.recordSale();
        Product savedProduct = productRepository.save(product);
        
        log.info("Sale recorded for {}. New Stock: {}, Velocity: {}", 
                 savedProduct.getSku(), savedProduct.getStockLevel(), savedProduct.getDemandVelocity());
        
        // Trigger checks for agentic loop
        checkTriggers(savedProduct);
        
        return savedProduct;
    }
    
    @Transactional
    public Product updateProductStock(Long id, Integer newStockLevel) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            return null;
        }
        
        Product product = productOpt.get();
        product.setStockLevel(newStockLevel);
        Product savedProduct = productRepository.save(product);
        
        log.info("Stock updated for {}. New Stock: {}", savedProduct.getSku(), newStockLevel);
        
        // If stock is below or at reorder threshold, fire agentic loop
        if (savedProduct.getStockLevel() <= savedProduct.getReorderThreshold() && savedProduct.getStockLevel() > 0) {
            eventPublisher.publishProductChange(savedProduct, TriggerReason.INVENTORY_LOW);
        }
        
        return savedProduct;
    }
    
    @Transactional
    public Product updateProductDemandVelocity(Long id, Integer newVelocity) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            return null;
        }
        
        Product product = productOpt.get();
        product.setDemandVelocity(newVelocity);
        Product savedProduct = productRepository.save(product);
        
        Double categoryAvg = productRepository.findAverageDemandVelocityByCategory(savedProduct.getCategory());
        if (categoryAvg != null && categoryAvg > 0 && savedProduct.getDemandVelocity() >= categoryAvg * 3) {
            eventPublisher.publishProductChange(savedProduct, TriggerReason.DEMAND_SPIKE);
        }
        
        return savedProduct;
    }
    
    private void checkTriggers(Product product) {
        // Trigger A: Low Inventory (Stock <= threshold and > 0)
        if (product.getStockLevel() <= product.getReorderThreshold() && product.getStockLevel() > 0) {
            log.info("Low stock trigger detected for SKU: {}", product.getSku());
            eventPublisher.publishProductChange(product, TriggerReason.INVENTORY_LOW);
        }
        
        // Trigger B: Demand Spike (Velocity >= 3x category average)
        Double categoryAvg = productRepository.findAverageDemandVelocityByCategory(product.getCategory());
        if (categoryAvg != null && categoryAvg > 0 && product.getDemandVelocity() >= categoryAvg * 3) {
            log.info("Demand spike trigger detected for SKU: {} (velocity {} vs category avg {})", 
                     product.getSku(), product.getDemandVelocity(), categoryAvg);
            eventPublisher.publishProductChange(product, TriggerReason.DEMAND_SPIKE);
        }
    }
    
    public ProductRepository getProductRepository() {
        return productRepository;
    }
    
    @Transactional
    public void initializeSeedData() {
        if (productRepository.count() > 0) {
            return;
        }
        
        log.info("Initializing StockPulse seed data (8 products)...");
        
        // Addendum A Products
        Product[] seedProducts = {
            new Product("SKU-ELEC-001", "Wireless Earbuds Pro", Category.ELECTRONICS, 
                       new BigDecimal("79.99"), 45, 20, 3, ProductStatus.ACTIVE),
            new Product("SKU-ELEC-002", "USB-C Hub 7-Port", Category.ELECTRONICS, 
                       new BigDecimal("34.99"), 120, 30, 1, ProductStatus.ACTIVE),
            new Product("SKU-APP-001", "Organic Cotton T-Shirt", Category.APPAREL, 
                       new BigDecimal("24.99"), 8, 15, 12, ProductStatus.PRICE_REVIEW_PENDING),
            new Product("SKU-APP-002", "Running Shorts — Navy", Category.APPAREL, 
                       new BigDecimal("39.99"), 55, 20, 2, ProductStatus.ACTIVE),
            new Product("SKU-HOME-001", "Ceramic Pour-Over Set", Category.HOME, 
                       new BigDecimal("49.99"), 22, 10, 4, ProductStatus.ACTIVE),
            new Product("SKU-HOME-002", "LED Desk Lamp — Dimmable", Category.HOME, 
                       new BigDecimal("59.99"), 0, 15, 0, ProductStatus.OUT_OF_STOCK),
            new Product("SKU-ELEC-003", "Portable Charger 20K", Category.ELECTRONICS, 
                       new BigDecimal("44.99"), 18, 25, 8, ProductStatus.ACTIVE),
            new Product("SKU-APP-003", "Hoodie — Heather Grey", Category.APPAREL, 
                       new BigDecimal("54.99"), 11, 12, 15, ProductStatus.ACTIVE)
        };
        
        for (Product product : seedProducts) {
            Product saved = productRepository.save(product);
            
            // For PRD-003 (T-shirt, stock 8 < threshold 15), seed initial pending suggestions for immediate demo!
            if ("SKU-APP-001".equals(saved.getSku())) {
                suggestionService.createPricingSuggestion(saved.getId(), TriggerReason.INVENTORY_LOW);
                suggestionService.createReorderSuggestion(saved.getId(), TriggerReason.INVENTORY_LOW);
            }
        }
        
        log.info("StockPulse seed data successfully initialized.");
    }

    @Transactional
    public void resetSeedData() {
        log.info("Resetting StockPulse data to baseline seed...");
        pricingSuggestionRepository.deleteAll();
        reorderSuggestionRepository.deleteAll();
        productRepository.deleteAll();
        initializeSeedData();
    }
}