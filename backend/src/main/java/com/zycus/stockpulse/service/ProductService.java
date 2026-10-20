package com.zycus.stockpulse.service;

import com.zycus.stockpulse.entity.Product;
import com.zycus.stockpulse.entity.ProductStatus;
import com.zycus.stockpulse.entity.Category;
import com.zycus.stockpulse.entity.TriggerReason;
import com.zycus.stockpulse.repository.ProductRepository;
import com.zycus.stockpulse.service.ProductEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductEventPublisher eventPublisher;
    
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
    
    public Optional<Product> getProductBySku(String sku) {
        return productRepository.findBySku(sku);
    }
    
    public Product updateProductStock(Long id, Integer newStockLevel) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            Integer oldStockLevel = product.getStockLevel();
            product.setStockLevel(newStockLevel);
            Product savedProduct = productRepository.save(product);
            
            // Check for triggers
            checkTriggersForStockChange(savedProduct, oldStockLevel);
            
            return savedProduct;
        }
        return null;
    }
    
    public Product updateProductDemandVelocity(Long id, Integer newVelocity) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            Integer oldVelocity = product.getDemandVelocity();
            product.setDemandVelocity(newVelocity);
            Product savedProduct = productRepository.save(product);
            
            // Check for triggers
            checkTriggersForDemandChange(savedProduct, oldVelocity);
            
            return savedProduct;
        }
        return null;
    }
    
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }
    
    public void initializeSeedData() {
        // Check if data already exists
        if (productRepository.count() > 0) {
            return;
        }
        
        // Seed the 8 products as specified
        Product[] seedProducts = {
            new Product("PRD-001", "Wireless Earbuds Pro", Category.ELECTRONICS, 
                       new BigDecimal("79.99"), 45, 20, 3),
            new Product("PRD-002", "USB-C Hub 7-Port", Category.ELECTRONICS, 
                       new BigDecimal("34.99"), 120, 30, 1),
            new Product("PRD-003", "Organic Cotton T-Shirt", Category.APPAREL, 
                       new BigDecimal("24.99"), 8, 15, 12),
            new Product("PRD-004", "Running Shorts — Navy", Category.APPAREL, 
                       new BigDecimal("39.99"), 55, 20, 2),
            new Product("PRD-005", "Ceramic Pour-Over Set", Category.HOME, 
                       new BigDecimal("49.99"), 22, 10, 4),
            new Product("PRD-006", "LED Desk Lamp — Dimmable", Category.HOME, 
                       new BigDecimal("59.99"), 0, 15, 0),
            new Product("PRD-007", "Portable Charger 20K", Category.ELECTRONICS, 
                       new BigDecimal("44.99"), 18, 25, 8),
            new Product("PRD-008", "Hoodie — Heather Grey", Category.APPAREL, 
                       new BigDecimal("54.99"), 11, 12, 15)
        };
        
        for (Product product : seedProducts) {
            if (product.getStockLevel() <= 0) {
                product.setStatus(ProductStatus.OUT_OF_STOCK);
            }
            productRepository.save(product);
        }
    }
    
    // Getter for repository (needed by ProductController)
    public ProductRepository getProductRepository() {
        return productRepository;
    }
    
    private void checkTriggersForStockChange(Product product, Integer oldStockLevel) {
        // Check for inventory low trigger
        if (product.getStockLevel() <= product.getReorderThreshold() && 
            oldStockLevel > product.getReorderThreshold()) {
            eventPublisher.publishProductChange(product, TriggerReason.INVENTORY_LOW);
        }
    }
    
    private void checkTriggersForDemandChange(Product product, Integer oldVelocity) {
        // Check for demand spike trigger
        Double categoryAvgVelocity = productRepository.findAverageDemandVelocityByCategory(product.getCategory());
        if (categoryAvgVelocity != null && categoryAvgVelocity > 0) {
            if (product.getDemandVelocity() >= categoryAvgVelocity * 3 && 
                oldVelocity < categoryAvgVelocity * 3) {
                eventPublisher.publishProductChange(product, TriggerReason.DEMAND_SPIKE);
            }
        }
    }
}