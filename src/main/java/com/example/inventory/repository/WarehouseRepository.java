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
    

    Optional<Warehouse> findByName(String name);
    
    List<Warehouse> findByLocationContainingIgnoreCase(String location);
    
    @Query("SELECT DISTINCT w FROM Warehouse w JOIN w.products p")
    List<Warehouse> findWarehousesWithProducts();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.warehouse.id = :warehouseId")
    Long countProductsInWarehouse(@Param("warehouseId") Long warehouseId);
    
    @Query("SELECT DISTINCT w FROM Warehouse w JOIN w.products p WHERE p.stockQuantity <= p.minStockLevel")
    List<Warehouse> findWarehousesWithLowStockProducts();
}
