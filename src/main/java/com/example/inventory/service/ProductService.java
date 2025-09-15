package com.example.inventory.service;

import com.example.inventory.exception.OutOfStockException;
import com.example.inventory.model.Product;
import com.example.inventory.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {
    
    private final ProductRepository productRepository;
    
    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    // CRUD Operations
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }
    
    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySku(sku);
    }
    
    public List<Product> findAll() {
        return productRepository.findAll();
    }
    
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
    
    // Functional Programming: Stream operations for filtering and analytics
    
    // Filter products by low stock using streams
    public List<Product> getLowStockProducts() {
        return findAll().stream()
                .filter(product -> product.getStockQuantity() <= product.getMinStockLevel())
                .sorted(Comparator.comparing(Product::getStockQuantity))
                .collect(Collectors.toList());
    }
    
    // Filter products by category using functional approach
    public List<Product> getProductsByCategory(String category) {
        return findAll().stream()
                .filter(product -> category.equalsIgnoreCase(product.getCategory()))
                .collect(Collectors.toList());
    }
    
    // Filter products by custom criteria using high-order functions
    public List<Product> filterProducts(Predicate<Product> criteria) {
        return findAll().stream()
                .filter(criteria)
                .collect(Collectors.toList());
    }
    
    // Map products to specific attributes using function mapping
    public <T> List<T> mapProducts(Function<Product, T> mapper) {
        return findAll().stream()
                .map(mapper)
                .collect(Collectors.toList());
    }
    
    // Analytics: Aggregate operations using streams
    
    // Calculate total inventory value
    public BigDecimal calculateTotalInventoryValue() {
        return findAll().stream()
                .map(product -> product.getPrice().multiply(new BigDecimal(product.getStockQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // Calculate inventory value by category
    public Map<String, BigDecimal> calculateInventoryValueByCategory() {
        return findAll().stream()
                .collect(Collectors.groupingBy(
                        Product::getCategory,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                product -> product.getPrice().multiply(new BigDecimal(product.getStockQuantity())),
                                BigDecimal::add
                        )
                ));
    }
    
    // Get product count by category
    public Map<String, Long> getProductCountByCategory() {
        return findAll().stream()
                .collect(Collectors.groupingBy(
                        Product::getCategory,
                        Collectors.counting()
                ));
    }
    
    // Find top N most expensive products
    public List<Product> getTopExpensiveProducts(int limit) {
        return findAll().stream()
                .sorted(Comparator.comparing(Product::getPrice).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    // Find products with stock below threshold
    public List<Product> getProductsBelowStockThreshold(int threshold) {
        return findAll().stream()
                .filter(product -> product.getStockQuantity() < threshold)
                .collect(Collectors.toList());
    }
    
    // Business Logic: Stock management with exception handling
    
    @Transactional
    public void updateStock(Long productId, int quantity) {
        Product product = findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));
        
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        
        product.updateStock(quantity);
        productRepository.save(product);
    }
    
    @Transactional
    public void reduceStock(Long productId, int quantity) {
        Product product = findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));
        
        if (product.getStockQuantity() < quantity) {
            throw new OutOfStockException(
                "Insufficient stock for product: " + product.getName() + 
                ". Available: " + product.getStockQuantity() + 
                ", Requested: " + quantity
            );
        }
        
        product.reduceStock(quantity);
        productRepository.save(product);
    }
    
    @Transactional
    public void increaseStock(Long productId, int quantity) {
        Product product = findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));
        
        product.increaseStock(quantity);
        productRepository.save(product);
    }
    
    // Search functionality
    public List<Product> searchProducts(String searchTerm) {
        return findAll().stream()
                .filter(product -> 
                    product.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    product.getDescription() != null && product.getDescription().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    product.getSku().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    product.getCategory().toLowerCase().contains(searchTerm.toLowerCase())
                )
                .collect(Collectors.toList());
    }
    
    // Alerts: Generate low stock alerts
    public List<String> generateLowStockAlerts() {
        return getLowStockProducts().stream()
                .map(product -> String.format(
                    "LOW STOCK ALERT: %s (SKU: %s) - Current Stock: %d, Min Level: %d",
                    product.getName(),
                    product.getSku(),
                    product.getStockQuantity(),
                    product.getMinStockLevel()
                ))
                .collect(Collectors.toList());
    }
    
    // Categories management
    public List<String> getAllCategories() {
        return findAll().stream()
                .map(Product::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
