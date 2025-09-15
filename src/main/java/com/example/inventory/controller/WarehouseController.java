package com.example.inventory.controller;

import com.example.inventory.model.Warehouse;
import com.example.inventory.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/warehouses")
@CrossOrigin(origins = "*")
public class WarehouseController {
    
    private final WarehouseService warehouseService;
    
    @Autowired
    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }
    
    // GET /api/warehouses - Get all warehouses
    @GetMapping
    public ResponseEntity<List<Warehouse>> getAllWarehouses() {
        List<Warehouse> warehouses = warehouseService.findAll();
        return ResponseEntity.ok(warehouses);
    }
    
    // GET /api/warehouses/{id} - Get warehouse by ID
    @GetMapping("/{id}")
    public ResponseEntity<Warehouse> getWarehouseById(@PathVariable Long id) {
        Optional<Warehouse> warehouse = warehouseService.findById(id);
        return warehouse.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }
    
    // GET /api/warehouses/name/{name} - Get warehouse by name
    @GetMapping("/name/{name}")
    public ResponseEntity<Warehouse> getWarehouseByName(@PathVariable String name) {
        Optional<Warehouse> warehouse = warehouseService.findByName(name);
        return warehouse.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }
    
    // POST /api/warehouses - Create new warehouse
    @PostMapping
    public ResponseEntity<Warehouse> createWarehouse(@Valid @RequestBody Warehouse warehouse) {
        Warehouse savedWarehouse = warehouseService.saveWarehouse(warehouse);
        return new ResponseEntity<>(savedWarehouse, HttpStatus.CREATED);
    }
    
    // PUT /api/warehouses/{id} - Update warehouse
    @PutMapping("/{id}")
    public ResponseEntity<Warehouse> updateWarehouse(@PathVariable Long id, @Valid @RequestBody Warehouse warehouse) {
        Optional<Warehouse> existingWarehouse = warehouseService.findById(id);
        if (existingWarehouse.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        warehouse.setId(id);
        Warehouse updatedWarehouse = warehouseService.saveWarehouse(warehouse);
        return ResponseEntity.ok(updatedWarehouse);
    }
    
    // DELETE /api/warehouses/{id} - Delete warehouse
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) {
        try {
            warehouseService.deleteWarehouse(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // GET /api/warehouses/search - Search warehouses
    @GetMapping("/search")
    public ResponseEntity<List<Warehouse>> searchWarehouses(@RequestParam String q) {
        List<Warehouse> warehouses = warehouseService.searchWarehouses(q);
        return ResponseEntity.ok(warehouses);
    }
    
    // GET /api/warehouses/search-by-location - Search warehouses by location
    @GetMapping("/search-by-location")
    public ResponseEntity<List<Warehouse>> searchWarehousesByLocation(@RequestParam String location) {
        List<Warehouse> warehouses = warehouseService.searchWarehousesByLocation(location);
        return ResponseEntity.ok(warehouses);
    }
    
    // GET /api/warehouses/low-stock - Get warehouses with low stock products
    @GetMapping("/low-stock")
    public ResponseEntity<List<Warehouse>> getWarehousesWithLowStock() {
        List<Warehouse> warehouses = warehouseService.getWarehousesWithLowStock();
        return ResponseEntity.ok(warehouses);
    }
    
    // GET /api/warehouses/names - Get warehouse names only
    @GetMapping("/names")
    public ResponseEntity<List<String>> getWarehouseNames() {
        List<String> names = warehouseService.getWarehouseNames();
        return ResponseEntity.ok(names);
    }
    
    // GET /api/warehouses/{id}/utilization - Get warehouse utilization details
    @GetMapping("/{id}/utilization")
    public ResponseEntity<Map<String, Object>> getWarehouseUtilization(@PathVariable Long id) {
        try {
            Map<String, Object> utilization = warehouseService.getWarehouseUtilization(id);
            return ResponseEntity.ok(utilization);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // GET /api/warehouses/analytics/inventory-value - Get inventory value by warehouse
    @GetMapping("/analytics/inventory-value")
    public ResponseEntity<Map<String, BigDecimal>> getInventoryValueByWarehouse() {
        Map<String, BigDecimal> inventoryValue = warehouseService.calculateInventoryValueByWarehouse();
        return ResponseEntity.ok(inventoryValue);
    }
    
    // GET /api/warehouses/analytics/product-count - Get product count by warehouse
    @GetMapping("/analytics/product-count")
    public ResponseEntity<Map<String, Long>> getProductCountByWarehouse() {
        Map<String, Long> productCount = warehouseService.getProductCountByWarehouse();
        return ResponseEntity.ok(productCount);
    }
    
    // GET /api/warehouses/analytics/grouped-by-location - Get warehouses grouped by location
    @GetMapping("/analytics/grouped-by-location")
    public ResponseEntity<Map<String, List<Warehouse>>> getWarehousesGroupedByLocation() {
        Map<String, List<Warehouse>> groupedWarehouses = warehouseService.groupWarehousesByLocation();
        return ResponseEntity.ok(groupedWarehouses);
    }
    
    // GET /api/warehouses/analytics/total-products - Get total products count across all warehouses
    @GetMapping("/analytics/total-products")
    public ResponseEntity<Long> getTotalProductsCount() {
        long totalProducts = warehouseService.getTotalProductsCount();
        return ResponseEntity.ok(totalProducts);
    }
    
    // GET /api/warehouses/summary - Get warehouse summary for dashboard
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getWarehouseSummary() {
        Map<String, Object> summary = warehouseService.getWarehouseSummary();
        return ResponseEntity.ok(summary);
    }
    
    // GET /api/warehouses/alerts - Get warehouse alerts
    @GetMapping("/alerts")
    public ResponseEntity<List<String>> getWarehouseAlerts() {
        List<String> alerts = warehouseService.generateWarehouseAlerts();
        return ResponseEntity.ok(alerts);
    }
}
