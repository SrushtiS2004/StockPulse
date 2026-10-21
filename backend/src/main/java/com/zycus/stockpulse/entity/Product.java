package com.zycus.stockpulse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(unique = true, nullable = false)
    private String sku;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Category category;
    
    @PositiveOrZero
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal currentPrice;
    
    // Sprint 2 seam: Margin floor & cost tracking placeholder
    @Column(precision = 10, scale = 2)
    private BigDecimal costPrice;
    
    @PositiveOrZero
    @Column(nullable = false)
    private Integer stockLevel;
    
    @PositiveOrZero
    @Column(nullable = false)
    private Integer reorderThreshold;
    
    @PositiveOrZero
    @Column(nullable = false)
    private Integer demandVelocity;
    
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private ProductStatus status;
    
    // Constructors
    public Product() {}
    
    public Product(String sku, String name, Category category, BigDecimal currentPrice, 
                   Integer stockLevel, Integer reorderThreshold, Integer demandVelocity) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.currentPrice = currentPrice;
        this.stockLevel = stockLevel;
        this.reorderThreshold = reorderThreshold;
        this.demandVelocity = demandVelocity;
        this.status = stockLevel <= 0 ? ProductStatus.OUT_OF_STOCK : ProductStatus.ACTIVE;
    }
    
    public Product(String sku, String name, Category category, BigDecimal currentPrice, 
                   Integer stockLevel, Integer reorderThreshold, Integer demandVelocity, ProductStatus status) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.currentPrice = currentPrice;
        this.stockLevel = stockLevel;
        this.reorderThreshold = reorderThreshold;
        this.demandVelocity = demandVelocity;
        this.status = status;
    }
    
    // Business logic helpers
    public void recordSale() {
        if (this.stockLevel > 0) {
            this.stockLevel -= 1;
        }
        this.demandVelocity += 1;
        if (this.stockLevel <= 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        } else if (this.stockLevel <= this.reorderThreshold) {
            this.status = ProductStatus.PRICE_REVIEW_PENDING;
        }
    }
    
    public void replenishStock(int quantity) {
        this.stockLevel += quantity;
        if (this.stockLevel > this.reorderThreshold) {
            this.status = ProductStatus.ACTIVE;
        } else if (this.stockLevel > 0 && this.status == ProductStatus.OUT_OF_STOCK) {
            this.status = ProductStatus.PRICE_REVIEW_PENDING;
        }
    }
    
    public void applyNewPrice(BigDecimal newPrice) {
        this.currentPrice = newPrice;
        if (this.status == ProductStatus.PRICE_REVIEW_PENDING) {
            if (this.stockLevel > this.reorderThreshold) {
                this.status = ProductStatus.ACTIVE;
            }
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSku() {
        return sku;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }
    
    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }
    
    public BigDecimal getCostPrice() {
        return costPrice;
    }
    
    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }
    
    public Integer getStockLevel() {
        return stockLevel;
    }
    
    public void setStockLevel(Integer stockLevel) {
        this.stockLevel = stockLevel;
        if (this.stockLevel <= 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        } else if (this.stockLevel <= this.reorderThreshold && this.status != ProductStatus.OUT_OF_STOCK) {
            this.status = ProductStatus.PRICE_REVIEW_PENDING;
        }
    }
    
    public Integer getReorderThreshold() {
        return reorderThreshold;
    }
    
    public void setReorderThreshold(Integer reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }
    
    public Integer getDemandVelocity() {
        return demandVelocity;
    }
    
    public void setDemandVelocity(Integer demandVelocity) {
        this.demandVelocity = demandVelocity;
    }
    
    public ProductStatus getStatus() {
        return status;
    }
    
    public void setStatus(ProductStatus status) {
        this.status = status;
    }
}