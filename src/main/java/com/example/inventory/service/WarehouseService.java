package com.example.inventory.service;

import com.example.inventory.model.Warehouse;
import com.example.inventory.model.Product;
import com.example.inventory.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Transactional
public class WarehouseService {
    
    private final WarehouseRepository warehouseRepository;
    
    @Autowired
    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }
    
    // CRUD Operations
    public Warehouse saveWarehouse(Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }
    
    public Optional<Warehouse> findById(Long id) {
        return warehouseRepository.findById(id);
    }
    
    public Warehouse getWarehouseById(Long id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found with ID: " + id));
    }
    
    public Optional<Warehouse> findByName(String name) {
        return warehouseRepository.findByName(name);
    }
    
    public List<Warehouse> findAll() {
        return warehouseRepository.findAll();
    }
    
    public void deleteWarehouse(Long id) {
        if (!warehouseRepository.existsById(id)) {
            throw new IllegalArgumentException("Warehouse not found with ID: " + id);
        }
        warehouseRepository.deleteById(id);
    }
    
    // Functional Programming: Stream operations for filtering and analytics
    
    // Filter warehouses by custom criteria
    public List<Warehouse> filterWarehouses(Predicate<Warehouse> criteria) {
        return findAll().stream()
                .filter(criteria)
                .collect(Collectors.toList());
    }
    
    // Map warehouses to specific attributes
    public <T> List<T> mapWarehouses(Function<Warehouse, T> mapper) {
        return findAll().stream()
                .map(mapper)
                .collect(Collectors.toList());
    }
    
    // Search warehouses by location
    public List<Warehouse> searchWarehousesByLocation(String location) {
        return findAll().stream()
                .filter(warehouse -> warehouse.getLocation().toLowerCase().contains(location.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    // Analytics: Aggregate operations using streams
    
    // Get warehouse names only
    public List<String> getWarehouseNames() {
        return findAll().stream()
                .map(Warehouse::getName)
                .sorted()
                .collect(Collectors.toList());
    }
    
    // Group warehouses by location (city from location string)
    public Map<String, List<Warehouse>> groupWarehousesByLocation() {
        return findAll().stream()
                .collect(Collectors.groupingBy(warehouse -> {
                    // Extract city from location (simplified - assumes last part is city)
                    String[] locationParts = warehouse.getLocation().split(",");
                    return locationParts.length > 0 ? 
                           locationParts[locationParts.length - 1].trim() : 
                           "Unknown";
                }));
    }
    
    // Calculate total products across all warehouses
    public long getTotalProductsCount() {
        return findAll().stream()
                .mapToLong(warehouse -> warehouse.getProducts().size())
                .sum();
    }
    
    // Calculate total inventory value per warehouse
    public Map<String, BigDecimal> calculateInventoryValueByWarehouse() {
        return findAll().stream()
                .collect(Collectors.toMap(
                        Warehouse::getName,
                        warehouse -> warehouse.getProducts().stream()
                                .map(product -> product.getPrice().multiply(new BigDecimal(product.getStockQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                ));
    }
    
    // Get warehouses with low stock products
    public List<Warehouse> getWarehousesWithLowStock() {
        return findAll().stream()
                .filter(warehouse -> warehouse.getProducts().stream()
                        .anyMatch(product -> product.getStockQuantity() <= product.getMinStockLevel()))
                .collect(Collectors.toList());
    }
    
    // Get product count per warehouse
    public Map<String, Long> getProductCountByWarehouse() {
        return findAll().stream()
                .collect(Collectors.toMap(
                        Warehouse::getName,
                        warehouse -> (long) warehouse.getProducts().size()
                ));
    }
    
    // Business Logic
    
    // Search functionality
    public List<Warehouse> searchWarehouses(String searchTerm) {
        return findAll().stream()
                .filter(warehouse -> 
                    warehouse.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    warehouse.getLocation().toLowerCase().contains(searchTerm.toLowerCase())
                )
                .collect(Collectors.toList());
    }
    
    // Validation
    public boolean isWarehouseNameUnique(String name, Long excludeId) {
        return findAll().stream()
                .filter(warehouse -> !warehouse.getId().equals(excludeId))
                .noneMatch(warehouse -> warehouse.getName().equalsIgnoreCase(name));
    }
    
    // Get warehouse utilization summary
    public Map<String, Object> getWarehouseUtilization(Long warehouseId) {
        Warehouse warehouse = getWarehouseById(warehouseId);
        
        List<Product> products = warehouse.getProducts();
        long totalProducts = products.size();
        long lowStockProducts = products.stream()
                .mapToLong(product -> product.getStockQuantity() <= product.getMinStockLevel() ? 1 : 0)
                .sum();
        
        BigDecimal totalValue = products.stream()
                .map(product -> product.getPrice().multiply(new BigDecimal(product.getStockQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return Map.of(
                "warehouseName", warehouse.getName(),
                "location", warehouse.getLocation(),
                "totalProducts", totalProducts,
                "lowStockProducts", lowStockProducts,
                "totalInventoryValue", totalValue,
                "lowStockPercentage", totalProducts > 0 ? (double) lowStockProducts / totalProducts * 100 : 0
        );
    }
    
    // Alert generation for warehouse issues
    public List<String> generateWarehouseAlerts() {
        return getWarehousesWithLowStock().stream()
                .map(warehouse -> {
                    long lowStockCount = warehouse.getProducts().stream()
                            .mapToLong(product -> product.getStockQuantity() <= product.getMinStockLevel() ? 1 : 0)
                            .sum();
                    return String.format(
                            "WAREHOUSE ALERT: %s has %d products with low stock",
                            warehouse.getName(),
                            lowStockCount
                    );
                })
                .collect(Collectors.toList());
    }
    
    // Get warehouse summary for dashboard
    public Map<String, Object> getWarehouseSummary() {
        List<Warehouse> warehouses = findAll();
        
        return Map.of(
                "totalWarehouses", warehouses.size(),
                "totalProducts", getTotalProductsCount(),
                "warehousesWithLowStock", getWarehousesWithLowStock().size(),
                "inventoryValueByWarehouse", calculateInventoryValueByWarehouse(),
                "productCountByWarehouse", getProductCountByWarehouse()
        );
    }
}
