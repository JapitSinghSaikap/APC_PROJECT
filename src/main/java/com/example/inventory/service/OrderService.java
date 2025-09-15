package com.example.inventory.service;

import com.example.inventory.exception.OutOfStockException;
import com.example.inventory.model.*;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;
    private final SupplierService supplierService;
    
    @Autowired
    public OrderService(OrderRepository orderRepository, 
                       OrderItemRepository orderItemRepository,
                       ProductService productService,
                       SupplierService supplierService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productService = productService;
        this.supplierService = supplierService;
    }
    
    // CRUD Operations
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }
    
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }
    
    public Order getOrderById(Long id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + id));
    }
    
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }
    
    public List<Order> findAll() {
        return orderRepository.findAll();
    }
    
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
    
    // Functional Programming: Stream operations for filtering and analytics
    
    // Filter orders by status
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return findAll().stream()
                .filter(order -> order.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    // Filter orders by type
    public List<Order> getOrdersByType(Order.OrderType type) {
        return findAll().stream()
                .filter(order -> order.getType() == type)
                .collect(Collectors.toList());
    }
    
    // Filter orders by custom criteria using high-order functions
    public List<Order> filterOrders(Predicate<Order> criteria) {
        return findAll().stream()
                .filter(criteria)
                .collect(Collectors.toList());
    }
    
    // Map orders to specific attributes
    public <T> List<T> mapOrders(Function<Order, T> mapper) {
        return findAll().stream()
                .map(mapper)
                .collect(Collectors.toList());
    }
    
    // Analytics: Aggregate operations using streams
    
    // Get pending orders using streams
    public List<Order> getPendingOrders() {
        return findAll().stream()
                .filter(Order::isPending)
                .sorted(Comparator.comparing(Order::getOrderDate))
                .collect(Collectors.toList());
    }
    
    // Get delayed orders using streams
    public List<Order> getDelayedOrders() {
        return findAll().stream()
                .filter(Order::isDelayed)
                .collect(Collectors.toList());
    }
    
    // Calculate total revenue using streams
    public BigDecimal calculateTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        return findAll().stream()
                .filter(order -> order.getOrderDate().isAfter(startDate) && order.getOrderDate().isBefore(endDate))
                .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // Get order count by status
    public Map<Order.OrderStatus, Long> getOrderCountByStatus() {
        return findAll().stream()
                .collect(Collectors.groupingBy(
                        Order::getStatus,
                        Collectors.counting()
                ));
    }
    
    // Get order count by type
    public Map<Order.OrderType, Long> getOrderCountByType() {
        return findAll().stream()
                .collect(Collectors.groupingBy(
                        Order::getType,
                        Collectors.counting()
                ));
    }
    
    // Get orders grouped by supplier
    public Map<String, List<Order>> getOrdersGroupedBySupplier() {
        return findAll().stream()
                .filter(order -> order.getSupplier() != null)
                .collect(Collectors.groupingBy(
                        order -> order.getSupplier().getName()
                ));
    }
    
    // Business Logic: Order processing with collections and exception handling
    
    @Transactional
    public Order createOrder(Order.OrderType type, Long supplierId, List<OrderItem> items) {
        // Validate supplier if provided
        Supplier supplier = null;
        if (supplierId != null) {
            supplier = supplierService.getSupplierById(supplierId);
        }
        
        // Create order
        Order order = new Order(type, supplier);
        order = saveOrder(order);
        
        // Add items to order
        for (OrderItem item : items) {
            // Validate product exists
            Product product = productService.findById(item.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            
            item.setProduct(product);
            item.setOrder(order);
            orderItemRepository.save(item);
        }
        
        // Calculate total amount
        order.calculateTotalAmount();
        return saveOrder(order);
    }
    
    @Transactional
    public Order processOrder(Long orderId) {
        Order order = getOrderById(orderId);
        
        if (order.getType() == Order.OrderType.SALE) {
            // For sales orders, reduce stock
            for (OrderItem item : order.getOrderItems()) {
                try {
                    productService.reduceStock(item.getProduct().getId(), item.getQuantity());
                } catch (OutOfStockException e) {
                    throw new OutOfStockException("Cannot process order " + order.getOrderNumber() + ": " + e.getMessage());
                }
            }
        }
        
        order.confirm();
        return saveOrder(order);
    }
    
    @Transactional
    public Order shipOrder(Long orderId) {
        Order order = getOrderById(orderId);
        
        if (order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order must be confirmed before shipping");
        }
        
        order.ship();
        return saveOrder(order);
    }
    
    @Transactional
    public Order deliverOrder(Long orderId) {
        Order order = getOrderById(orderId);
        
        if (order.getStatus() != Order.OrderStatus.SHIPPED) {
            throw new IllegalStateException("Order must be shipped before delivery");
        }
        
        if (order.getType() == Order.OrderType.PURCHASE) {
            // For purchase orders, increase stock when delivered
            for (OrderItem item : order.getOrderItems()) {
                productService.increaseStock(item.getProduct().getId(), item.getQuantity());
            }
        }
        
        order.deliver();
        return saveOrder(order);
    }
    
    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = getOrderById(orderId);
        
        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel delivered order");
        }
        
        // If order was processed and is a sale order, restore stock
        if (order.getStatus() != Order.OrderStatus.PENDING && order.getType() == Order.OrderType.SALE) {
            for (OrderItem item : order.getOrderItems()) {
                productService.increaseStock(item.getProduct().getId(), item.getQuantity());
            }
        }
        
        order.cancel();
        return saveOrder(order);
    }
    
    // Search functionality
    public List<Order> searchOrders(String searchTerm) {
        return findAll().stream()
                .filter(order -> 
                    order.getOrderNumber().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    (order.getSupplier() != null && order.getSupplier().getName().toLowerCase().contains(searchTerm.toLowerCase()))
                )
                .collect(Collectors.toList());
    }
    
    // Alert generation for order issues
    public List<String> generateOrderAlerts() {
        List<String> alerts = new ArrayList<>();
        
        // Pending orders alert
        long pendingCount = getPendingOrders().size();
        if (pendingCount > 0) {
            alerts.add(String.format("PENDING ORDERS: %d orders awaiting processing", pendingCount));
        }
        
        // Delayed orders alert
        List<Order> delayedOrders = getDelayedOrders();
        if (!delayedOrders.isEmpty()) {
            alerts.add(String.format("DELAYED ORDERS: %d orders past expected delivery date", delayedOrders.size()));
            alerts.addAll(delayedOrders.stream()
                    .map(order -> String.format("  - Order %s (Expected: %s)", 
                            order.getOrderNumber(), 
                            order.getExpectedDeliveryDate()))
                    .collect(Collectors.toList()));
        }
        
        return alerts;
    }
    
    // Recent orders (last 10)
    public List<Order> getRecentOrders() {
        return findAll().stream()
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }
    
    // High-order function for applying discounts
    public Function<BigDecimal, BigDecimal> createDiscountFunction(double discountPercentage) {
        return amount -> amount.multiply(BigDecimal.valueOf(1 - discountPercentage / 100));
    }
    
    // Apply discount to order
    @Transactional
    public Order applyDiscount(Long orderId, double discountPercentage) {
        Order order = getOrderById(orderId);
        Function<BigDecimal, BigDecimal> discountFunction = createDiscountFunction(discountPercentage);
        
        BigDecimal discountedAmount = discountFunction.apply(order.getTotalAmount());
        order.setTotalAmount(discountedAmount);
        
        return saveOrder(order);
    }
}
