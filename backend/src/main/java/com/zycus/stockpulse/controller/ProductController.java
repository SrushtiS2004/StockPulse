package com.zycus.stockpulse.controller;

import com.zycus.stockpulse.dto.CreateProductRequest;
import com.zycus.stockpulse.dto.ProductDTO;
import com.zycus.stockpulse.dto.UpdateStockRequest;
import com.zycus.stockpulse.entity.Product;
import com.zycus.stockpulse.entity.ProductStatus;
import com.zycus.stockpulse.entity.Category;
import com.zycus.stockpulse.service.ProductService;
import com.zycus.stockpulse.service.SuggestionService;
import com.zycus.stockpulse.entity.TriggerReason;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private SuggestionService suggestionService;
    
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductRequest request) {
        Product product = new Product(
            request.getSku(),
            request.getName(),
            request.getCategory(),
            request.getCurrentPrice(),
            request.getStockLevel(),
            request.getReorderThreshold(),
            request.getDemandVelocity()
        );
        
        Product savedProduct = productService.createProduct(product);
        return new ResponseEntity<>(convertToDTO(savedProduct), HttpStatus.CREATED);
    }
    
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getProducts(
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) Category category) {
        
        List<Product> products;
        if (status != null && category != null) {
            products = productService.getProductsByStatusAndCategory(status, category);
        } else if (status != null) {
            products = productService.getProductsByStatus(status);
        } else if (category != null) {
            products = productService.getProductsByCategory(category);
        } else {
            products = productService.getAllProducts();
        }
        
        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(productDTOs);
    }
    
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductDTO> updateProductStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockRequest request) {
        
        Product updatedProduct = productService.updateProductStock(id, request.getStockLevel());
        if (updatedProduct != null) {
            return ResponseEntity.ok(convertToDTO(updatedProduct));
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/{id}/orders")
    public ResponseEntity<ProductDTO> recordOrder(@PathVariable Long id) {
        // Update demand velocity (increment by 1 for this order)
        Product product = productService.updateProductDemandVelocity(id, 
            productService.getProductById(id).map(Product::getDemandVelocity).orElse(0) + 1);
        
        if (product != null) {
            // Check if this triggers any suggestions
            checkTriggers(product);
            return ResponseEntity.ok(convertToDTO(product));
        }
        return ResponseEntity.notFound().build();
    }
    
    private void checkTriggers(Product product) {
        // Check for inventory low trigger
        if (product.getStockLevel() <= product.getReorderThreshold() && product.getStockLevel() > 0) {
            suggestionService.createPricingSuggestion(product.getId(), TriggerReason.INVENTORY_LOW);
            suggestionService.createReorderSuggestion(product.getId(), TriggerReason.INVENTORY_LOW);
        }
        
        // Check for demand spike trigger
        Double categoryAvgVelocity = productService.getProductRepository().findAverageDemandVelocityByCategory(product.getCategory());
        if (categoryAvgVelocity != null && categoryAvgVelocity > 0) {
            if (product.getDemandVelocity() >= categoryAvgVelocity * 3) {
                suggestionService.createPricingSuggestion(product.getId(), TriggerReason.DEMAND_SPIKE);
                suggestionService.createReorderSuggestion(product.getId(), TriggerReason.DEMAND_SPIKE);
            }
        }
    }
    
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setCategory(product.getCategory());
        dto.setCurrentPrice(product.getCurrentPrice());
        dto.setCostPrice(product.getCostPrice());
        dto.setStockLevel(product.getStockLevel());
        dto.setReorderThreshold(product.getReorderThreshold());
        dto.setDemandVelocity(product.getDemandVelocity());
        dto.setStatus(product.getStatus());
        return dto;
    }
}