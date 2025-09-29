package com.example.inventory.controller;

import com.example.inventory.model.Product;
import com.example.inventory.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    
    private final ProductService productService;
    
    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    // GET /api/products - Get all products
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.findAll();
        return ResponseEntity.ok(products);
    }
    
    // GET /api/products/{id} - Get product by ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.findById(id);
        return product.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    // GET /api/products/sku/{sku} - Get product by SKU
    @GetMapping("/sku/{sku}")
    public ResponseEntity<Product> getProductBySku(@PathVariable String sku) {
        Optional<Product> product = productService.findBySku(sku);
        return product.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    // POST /api/products - Create new product
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product savedProduct = productService.saveProduct(product);
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }
    
    // PUT /api/products/{id} - Update product
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        Optional<Product> existingProduct = productService.findById(id);
        if (existingProduct.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        product.setId(id);
        Product updatedProduct = productService.saveProduct(product);
        return ResponseEntity.ok(updatedProduct);
    }
    
    // DELETE /api/products/{id} - Delete product
   // Controller
@DeleteMapping("/{id}")
public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
    Optional<Product> product = productService.findById(id);
    if (product.isEmpty()) {
        return ResponseEntity.notFound().build();
    }
    try {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    } catch (DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body("Cannot delete product: it is linked to existing orders or references.");
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body("Internal server error while deleting product.");
    }
}

    
    // GET /api/products/low-stock - Get low stock products
    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts() {
        List<Product> products = productService.getLowStockProducts();
        return ResponseEntity.ok(products);
    }
    
    // GET /api/products/category/{category} - Get products by category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        List<Product> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }
    
    // GET /api/products/search - Search products
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String q) {
        List<Product> products = productService.searchProducts(q);
        return ResponseEntity.ok(products);
    }
    
    // PUT /api/products/{id}/stock - Update product stock
    @PutMapping("/{id}/stock")
    public ResponseEntity<Product> updateStock(@PathVariable Long id, @RequestParam int quantity) {
        try {
            productService.updateStock(id, quantity);
            Optional<Product> updatedProduct = productService.findById(id);
            return updatedProduct.map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // PUT /api/products/{id}/reduce-stock - Reduce product stock
    @PutMapping("/{id}/reduce-stock")
    public ResponseEntity<Product> reduceStock(@PathVariable Long id, @RequestParam int quantity) {
        try {
            productService.reduceStock(id, quantity);
            Optional<Product> updatedProduct = productService.findById(id);
            return updatedProduct.map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // PUT /api/products/{id}/increase-stock - Increase product stock
    @PutMapping("/{id}/increase-stock")
    public ResponseEntity<Product> increaseStock(@PathVariable Long id, @RequestParam int quantity) {
        try {
            productService.increaseStock(id, quantity);
            Optional<Product> updatedProduct = productService.findById(id);
            return updatedProduct.map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // GET /api/products/analytics/inventory-value - Get total inventory value
    @GetMapping("/analytics/inventory-value")
    public ResponseEntity<BigDecimal> getTotalInventoryValue() {
        BigDecimal totalValue = productService.calculateTotalInventoryValue();
        return ResponseEntity.ok(totalValue);
    }
    
    // GET /api/products/analytics/inventory-by-category - Get inventory value by category
    @GetMapping("/analytics/inventory-by-category")
    public ResponseEntity<Map<String, BigDecimal>> getInventoryValueByCategory() {
        Map<String, BigDecimal> valueByCategory = productService.calculateInventoryValueByCategory();
        return ResponseEntity.ok(valueByCategory);
    }
    
    // GET /api/products/analytics/count-by-category - Get product count by category
    @GetMapping("/analytics/count-by-category")
    public ResponseEntity<Map<String, Long>> getProductCountByCategory() {
        Map<String, Long> countByCategory = productService.getProductCountByCategory();
        return ResponseEntity.ok(countByCategory);
    }
    
    // GET /api/products/categories - Get all categories
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = productService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    // GET /api/products/alerts - Get low stock alerts
    @GetMapping("/alerts")
    public ResponseEntity<List<String>> getLowStockAlerts() {
        List<String> alerts = productService.generateLowStockAlerts();
        return ResponseEntity.ok(alerts);
    }
    
    // GET /api/products/top-expensive - Get top expensive products
    @GetMapping("/top-expensive")
    public ResponseEntity<List<Product>> getTopExpensiveProducts(@RequestParam(defaultValue = "10") int limit) {
        List<Product> products = productService.getTopExpensiveProducts(limit);
        return ResponseEntity.ok(products);
    }
}
