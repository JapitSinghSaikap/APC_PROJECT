package com.example.inventory.repository;

import com.example.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Find product by SKU
    Optional<Product> findBySku(String sku);
    
    // Find products by category
    List<Product> findByCategory(String category);
    
    // Find products by warehouse
    List<Product> findByWarehouseId(Long warehouseId);
    
    // Find products by supplier
    List<Product> findBySupplierId(Long supplierId);
    
    // Find low stock products (stock <= min stock level)
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.minStockLevel")
    List<Product> findLowStockProducts();
    
    // Find products by name containing (case insensitive)
    List<Product> findByNameContainingIgnoreCase(String name);
    
    // Find products with stock quantity less than specified amount
    @Query("SELECT p FROM Product p WHERE p.stockQuantity < :quantity")
    List<Product> findProductsWithStockLessThan(@Param("quantity") Integer quantity);
    
    // Find products by category and warehouse
    List<Product> findByCategoryAndWarehouseId(String category, Long warehouseId);
    
    // Count products by category
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category = :category")
    Long countProductsByCategory(@Param("category") String category);
    
    // Find all distinct categories
    @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
    List<String> findAllCategories();
}
