package com.example.inventory.controller;

//used for getting the data and statistics for the dashboard
import com.example.inventory.service.ProductService;
import com.example.inventory.service.OrderService;
import com.example.inventory.service.SupplierService;
import com.example.inventory.service.WarehouseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
//for rest controller and request mapping jo ismein use huye hain
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// import java.util.stream.Stream;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final ProductService productService;
    private final OrderService orderService;
    private final SupplierService supplierService;
    private final WarehouseService warehouseService;
    
    @Autowired
    public DashboardController(ProductService productService, 
                              OrderService orderService,
                              SupplierService supplierService,
                              WarehouseService warehouseService) {
        this.productService = productService;
        this.orderService = orderService;
        this.supplierService = supplierService;
        this.warehouseService = warehouseService;
    }
    
    // GET /api/dashboard/summary - Get overall system summary
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Basic counts hai yeh
        summary.put("totalProducts", productService.findAll().size());
        summary.put("totalOrders", orderService.findAll().size());
        summary.put("totalSuppliers", supplierService.findAll().size());
        summary.put("totalWarehouses", warehouseService.findAll().size());
        
     
        summary.put("lowStockProductsCount", productService.getLowStockProducts().size());
        summary.put("pendingOrdersCount", orderService.getPendingOrders().size());
        summary.put("delayedOrdersCount", orderService.getDelayedOrders().size());
        summary.put("activeSuppliers", supplierService.getActiveSuppliers().size());
       
        summary.put("totalInventoryValue", productService.calculateTotalInventoryValue());
        
        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
        LocalDateTime now = LocalDateTime.now();
        summary.put("weeklyRevenue", orderService.calculateTotalRevenue(lastWeek, now));
        
        return ResponseEntity.ok(summary);
    }
    
    // GET /api/dashboard/alerts - Get all system alerts
    @GetMapping("/alerts")
    public ResponseEntity<Map<String, List<String>>> getAllAlerts() {
        Map<String, List<String>> alerts = new HashMap<>();
        
        alerts.put("productAlerts", productService.generateLowStockAlerts());
        alerts.put("orderAlerts", orderService.generateOrderAlerts());
        alerts.put("supplierAlerts", supplierService.generateSupplierAlerts());
        alerts.put("warehouseAlerts", warehouseService.generateWarehouseAlerts());
        
        return ResponseEntity.ok(alerts);
    }
    
    // GET /api/dashboard/critical-alerts - Get only critical alerts (combined)
    // @GetMapping("/critical-alerts")
    // public ResponseEntity<List<String>> getCriticalAlerts() {
    //     List<String> criticalAlerts = Stream.of(
    //             productService.generateLowStockAlerts(),
    //             orderService.generateOrderAlerts(),
    //             supplierService.generateSupplierAlerts(),
    //             warehouseService.generateWarehouseAlerts()
    //     )
    //     .flatMap(List::stream)
    //     .toList();
        
    //     return ResponseEntity.ok(criticalAlerts);
    // }
    
    // GET /api/dashboard/analytics - Get comprehensive analytics
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        analytics.put("inventoryValueByCategory", productService.calculateInventoryValueByCategory());
        analytics.put("productCountByCategory", productService.getProductCountByCategory());
        analytics.put("topExpensiveProducts", productService.getTopExpensiveProducts(5));
        analytics.put("orderCountByStatus", orderService.getOrderCountByStatus());
        analytics.put("orderCountByType", orderService.getOrderCountByType());
        analytics.put("recentOrders", orderService.getRecentOrders());
        analytics.put("supplierCountByStatus", supplierService.getSupplierCountByStatus());
        analytics.put("reliableSuppliers", supplierService.getReliableSuppliers());
        analytics.put("warehouseSummary", warehouseService.getWarehouseSummary());
        analytics.put("inventoryValueByWarehouse", warehouseService.calculateInventoryValueByWarehouse());
        
        return ResponseEntity.ok(analytics);//ok means http 200
    }
    
 
    
    // GET /api/dashboard/quick-stats - Get quick statistics for widgets
    @GetMapping("/quick-stats")
    public ResponseEntity<Map<String, Object>> getQuickStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Inventory stats
        stats.put("totalInventoryValue", productService.calculateTotalInventoryValue());
        stats.put("lowStockProducts", productService.getLowStockProducts().size());
        stats.put("totalProducts", productService.findAll().size());
        stats.put("categories", productService.getAllCategories().size());
        
        // Order stats
        stats.put("pendingOrders", orderService.getPendingOrders().size());
        stats.put("delayedOrders", orderService.getDelayedOrders().size());
        stats.put("totalOrders", orderService.findAll().size());
        
        // Supplier stats
        stats.put("activeSuppliers", supplierService.getActiveSuppliers().size());
        stats.put("totalSuppliers", supplierService.findAll().size());
        
        // Warehouse stats
        stats.put("totalWarehouses", warehouseService.findAll().size());
        stats.put("warehousesWithLowStock", warehouseService.getWarehousesWithLowStock().size());
        
        return ResponseEntity.ok(stats);
    }
    
    // GET /api/dashboard/performance - Get system performance metrics
    // @GetMapping("/performance")
    // public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
    //     Map<String, Object> performance = new HashMap<>();
        
        // Calculate performance indicators
    //     long totalProducts = productService.findAll().size();
    //     long lowStockProducts = productService.getLowStockProducts().size();
        
    //     double stockHealthPercentage = totalProducts > 0 ? 
    //         ((double)(totalProducts - lowStockProducts) / totalProducts) * 100 : 100;
        
    //     long totalOrders = orderService.findAll().size();
    //     long delayedOrders = orderService.getDelayedOrders().size();
        
    //     double orderPerformancePercentage = totalOrders > 0 ?
    //         ((double)(totalOrders - delayedOrders) / totalOrders) * 100 : 100;
        
    //     long totalSuppliers = supplierService.findAll().size();
    //     long activeSuppliers = supplierService.getActiveSuppliers().size();
        
    //     double supplierReliabilityPercentage = totalSuppliers > 0 ?
    //         ((double)activeSuppliers / totalSuppliers) * 100 : 100;
        
    //     performance.put("stockHealthPercentage", Math.round(stockHealthPercentage * 100.0) / 100.0);
    //     performance.put("orderPerformancePercentage", Math.round(orderPerformancePercentage * 100.0) / 100.0);
    //     performance.put("supplierReliabilityPercentage", Math.round(supplierReliabilityPercentage * 100.0) / 100.0);
        
    //     // Overall system health score (average of key metrics)
    //     double overallHealthScore = (stockHealthPercentage + orderPerformancePercentage + supplierReliabilityPercentage) / 3;
    //     performance.put("overallHealthScore", Math.round(overallHealthScore * 100.0) / 100.0);
        
    //     return ResponseEntity.ok(performance);
    // }
}
