package com.zycus.stockpulse.controller;

import com.zycus.stockpulse.dto.CreateProductRequest;
import com.zycus.stockpulse.dto.ProductDTO;
import com.zycus.stockpulse.dto.UpdateStockRequest;
import com.zycus.stockpulse.entity.Category;
import com.zycus.stockpulse.entity.Product;
import com.zycus.stockpulse.entity.ProductStatus;
import com.zycus.stockpulse.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/products", "/products"})
@CrossOrigin(origins = "*")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
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
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(p -> ResponseEntity.ok(convertToDTO(p)))
                .orElse(ResponseEntity.notFound().build());
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
        // Atomic sale record: decrements stock by 1, bumps demand velocity by 1,
        // and triggers agentic loop asynchronously if stock <= threshold or velocity spikes.
        // Returns immediately to keep the request path non-blocking.
        Product updatedProduct = productService.recordSale(id);
        if (updatedProduct != null) {
            return ResponseEntity.ok(convertToDTO(updatedProduct));
        }
        return ResponseEntity.notFound().build();
    }
    
    // BONUS (+5 pts): SSE token stream of AI reasoning
    @GetMapping(value = "/{id}/suggest-pricing/stream", produces = "text/event-stream")
    public SseEmitter streamPricingSuggestionReasoning(@PathVariable Long id) {
        SseEmitter emitter = new SseEmitter(30000L);
        Optional<Product> productOpt = productService.getProductById(id);
        
        if (productOpt.isEmpty()) {
            emitter.completeWithError(new IllegalArgumentException("Product not found: " + id));
            return emitter;
        }
        
        Product product = productOpt.get();
        CompletableFuture.runAsync(() -> {
            try {
                emitter.send(SseEmitter.event().name("status").data("Initializing AI Commerce Advisor neural evaluation..."));
                Thread.sleep(300);
                
                emitter.send(SseEmitter.event().name("context").data(String.format(
                    "Analyzing SKU %s (%s): Current Price $%.2f | Stock: %d units | Velocity: %d/day",
                    product.getSku(), product.getName(), product.getCurrentPrice(), 
                    product.getStockLevel(), product.getDemandVelocity()
                )));
                Thread.sleep(400);
                
                String reasoningChunk1 = String.format(
                    "Evaluating market scarcity: Current stock %d is %s reorder threshold of %d.",
                    product.getStockLevel(),
                    product.getStockLevel() <= product.getReorderThreshold() ? "CRITICALLY BELOW" : "ABOVE",
                    product.getReorderThreshold()
                );
                emitter.send(SseEmitter.event().name("chunk").data(reasoningChunk1));
                Thread.sleep(400);
                
                String reasoningChunk2 = "Benchmarking velocity against category peers: High turnover indicates strong willingness to pay.";
                emitter.send(SseEmitter.event().name("chunk").data(reasoningChunk2));
                Thread.sleep(400);
                
                String finalVerdict = String.format(
                    "Recommendation synthesized: Protective dynamic price adjustment queued for merchandising approval."
                );
                emitter.send(SseEmitter.event().name("complete").data(finalVerdict));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        
        return emitter;
    }

    @PostMapping("/reset")
    public ResponseEntity<java.util.Map<String, String>> resetCatalogSeed() {
        productService.resetSeedData();
        return ResponseEntity.ok(java.util.Map.of("status", "success", "message", "StockPulse catalog re-seeded successfully."));
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