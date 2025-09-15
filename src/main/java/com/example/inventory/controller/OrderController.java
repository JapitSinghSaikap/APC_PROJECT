package com.example.inventory.controller;

import com.example.inventory.model.Order;
import com.example.inventory.model.OrderItem;
import com.example.inventory.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    private final OrderService orderService;
    
    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    // GET /api/orders - Get all orders
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.findAll();
        return ResponseEntity.ok(orders);
    }
    
    // GET /api/orders/{id} - Get order by ID
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Optional<Order> order = orderService.findById(id);
        return order.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    // GET /api/orders/number/{orderNumber} - Get order by order number
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<Order> getOrderByNumber(@PathVariable String orderNumber) {
        Optional<Order> order = orderService.findByOrderNumber(orderNumber);
        return order.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    // POST /api/orders - Create new order
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        try {
            Order order = orderService.createOrder(request.getType(), request.getSupplierId(), request.getItems());
            return new ResponseEntity<>(order, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("Error creating order: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    // PUT /api/orders/{id} - Update order
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @Valid @RequestBody Order order) {
        Optional<Order> existingOrder = orderService.findById(id);
        if (existingOrder.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        order.setId(id);
        Order updatedOrder = orderService.saveOrder(order);
        return ResponseEntity.ok(updatedOrder);
    }
    
    // DELETE /api/orders/{id} - Delete order
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        Optional<Order> order = orderService.findById(id);
        if (order.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
    
    // GET /api/orders/status/{status} - Get orders by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable String status) {
        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            List<Order> orders = orderService.getOrdersByStatus(orderStatus);
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // GET /api/orders/type/{type} - Get orders by type
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Order>> getOrdersByType(@PathVariable String type) {
        try {
            Order.OrderType orderType = Order.OrderType.valueOf(type.toUpperCase());
            List<Order> orders = orderService.getOrdersByType(orderType);
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // GET /api/orders/pending - Get pending orders
    @GetMapping("/pending")
    public ResponseEntity<List<Order>> getPendingOrders() {
        List<Order> orders = orderService.getPendingOrders();
        return ResponseEntity.ok(orders);
    }
    
    // GET /api/orders/delayed - Get delayed orders
    @GetMapping("/delayed")
    public ResponseEntity<List<Order>> getDelayedOrders() {
        List<Order> orders = orderService.getDelayedOrders();
        return ResponseEntity.ok(orders);
    }
    
    // GET /api/orders/recent - Get recent orders
    @GetMapping("/recent")
    public ResponseEntity<List<Order>> getRecentOrders() {
        List<Order> orders = orderService.getRecentOrders();
        return ResponseEntity.ok(orders);
    }
    
    // GET /api/orders/search - Search orders
    @GetMapping("/search")
    public ResponseEntity<List<Order>> searchOrders(@RequestParam String q) {
        List<Order> orders = orderService.searchOrders(q);
        return ResponseEntity.ok(orders);
    }
    
    // PUT /api/orders/{id}/process - Process order
    @PutMapping("/{id}/process")
    public ResponseEntity<Order> processOrder(@PathVariable Long id) {
        try {
            Order order = orderService.processOrder(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // PUT /api/orders/{id}/ship - Ship order
    @PutMapping("/{id}/ship")
    public ResponseEntity<Order> shipOrder(@PathVariable Long id) {
        try {
            Order order = orderService.shipOrder(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // PUT /api/orders/{id}/deliver - Deliver order
    @PutMapping("/{id}/deliver")
    public ResponseEntity<Order> deliverOrder(@PathVariable Long id) {
        try {
            Order order = orderService.deliverOrder(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // PUT /api/orders/{id}/cancel - Cancel order
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
        try {
            Order order = orderService.cancelOrder(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
  
    
    //  Calculate total revenue
    @GetMapping("/analytics/revenue")
    public ResponseEntity<BigDecimal> getTotalRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        BigDecimal revenue = orderService.calculateTotalRevenue(startDate, endDate);
        return ResponseEntity.ok(revenue);
    }
    
    //  Get order count by status (pending,confirmed)
    @GetMapping("/analytics/count-by-status")
    public ResponseEntity<Map<Order.OrderStatus, Long>> getOrderCountByStatus() {
        Map<Order.OrderStatus, Long> countByStatus = orderService.getOrderCountByStatus();
        return ResponseEntity.ok(countByStatus);
    }
    
    // Get order count by type (kist type ka saman hoga)
    @GetMapping("/analytics/count-by-type")
    public ResponseEntity<Map<Order.OrderType, Long>> getOrderCountByType() {
        Map<Order.OrderType, Long> countByType = orderService.getOrderCountByType();
        return ResponseEntity.ok(countByType);
    }
    
    //  Get orders grouped by supplier (konsa hoga)
    @GetMapping("/analytics/grouped-by-supplier")
    public ResponseEntity<Map<String, List<Order>>> getOrdersGroupedBySupplier() {
        Map<String, List<Order>> groupedOrders = orderService.getOrdersGroupedBySupplier();
        return ResponseEntity.ok(groupedOrders);
    }
    
    // GET /api/orders/alerts - Get order alerts
    @GetMapping("/alerts")
    public ResponseEntity<List<String>> getOrderAlerts() {
        List<String> alerts = orderService.generateOrderAlerts();
        return ResponseEntity.ok(alerts);
    }
    
    // DTO for creating orders with validation
    public static class CreateOrderRequest {
        private Order.OrderType type;
        private Long supplierId;
        private List<OrderItem> items;
        public Order.OrderType getType() { return type; }
        public void setType(Order.OrderType type) { this.type = type; }
        public Long getSupplierId() { return supplierId; }
        public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
        public List<OrderItem> getItems() { return items; }
        public void setItems(List<OrderItem> items) { this.items = items; }
    }
}
