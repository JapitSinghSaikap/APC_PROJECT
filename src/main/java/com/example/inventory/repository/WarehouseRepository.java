package com.example.inventory.repository;

import com.example.inventory.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    
    // Find warehouse by name
    Optional<Warehouse> findByName(String name);
    
    // Find warehouses by location containing (case insensitive)
    List<Warehouse> findByLocationContainingIgnoreCase(String location);
    
    // Find warehouses with products
    @Query("SELECT DISTINCT w FROM Warehouse w JOIN w.products p")
    List<Warehouse> findWarehousesWithProducts();
    
    // Count products in a warehouse
    @Query("SELECT COUNT(p) FROM Product p WHERE p.warehouse.id = :warehouseId")
    Long countProductsInWarehouse(@Param("warehouseId") Long warehouseId);
    
    // Find warehouses with low stock products
    @Query("SELECT DISTINCT w FROM Warehouse w JOIN w.products p WHERE p.stockQuantity <= p.minStockLevel")
    List<Warehouse> findWarehousesWithLowStockProducts();
}
