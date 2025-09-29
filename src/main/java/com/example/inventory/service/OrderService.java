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

    // ===================== CRUD =====================
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

    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = getOrderById(orderId);

        // delete associated order items first
        for (OrderItem item : order.getOrderItems()) {
            orderItemRepository.delete(item);
        }

        orderRepository.delete(order);
    }

    // ===================== Filtering & Mapping =====================
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return findAll().stream()
                .filter(order -> order.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Order> getOrdersByType(Order.OrderType type) {
        return findAll().stream()
                .filter(order -> order.getType() == type)
                .collect(Collectors.toList());
    }

    public List<Order> filterOrders(Predicate<Order> criteria) {
        return findAll().stream()
                .filter(criteria)
                .collect(Collectors.toList());
    }

    public <T> List<T> mapOrders(Function<Order, T> mapper) {
        return findAll().stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    // ===================== Alerts & Reports =====================
    public List<Order> getPendingOrders() {
        return findAll().stream()
                .filter(Order::isPending)
                .sorted(Comparator.comparing(Order::getOrderDate))
                .collect(Collectors.toList());
    }

    public List<Order> getDelayedOrders() {
        return findAll().stream()
                .filter(Order::isDelayed)
                .collect(Collectors.toList());
    }

    public BigDecimal calculateTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        return findAll().stream()
                .filter(order -> order.getOrderDate().isAfter(startDate) && order.getOrderDate().isBefore(endDate))
                .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<Order.OrderStatus, Long> getOrderCountByStatus() {
        return findAll().stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
    }

    public Map<Order.OrderType, Long> getOrderCountByType() {
        return findAll().stream()
                .collect(Collectors.groupingBy(Order::getType, Collectors.counting()));
    }

    public Map<String, List<Order>> getOrdersGroupedBySupplier() {
        return findAll().stream()
                .filter(order -> order.getSupplier() != null)
                .collect(Collectors.groupingBy(order -> order.getSupplier().getName()));
    }

    // ===================== CREATE / UPDATE =====================
    @Transactional
    public Order createOrder(Order.OrderType type, Long supplierId, List<OrderItem> items) {
        Supplier supplier = null;
        if (supplierId != null) {
            supplier = supplierService.getSupplierById(supplierId);
        }

        Order order = new Order(type, supplier);
        order = saveOrder(order);

        if (items != null) {
            for (OrderItem item : items) {
                Product product = productService.findById(item.getProduct().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found"));

                item.setProduct(product);
                item.setUnitPrice(product.getPrice());
                item.setOrder(order);

                orderItemRepository.save(item); // save individually
                order.addOrderItem(item);       // attach to order
            }
        }

        order.calculateTotalAmount();
        return saveOrder(order);
    }

    @Transactional
    public Order updateOrderItems(Long orderId, List<OrderItem> updatedItems) {
        Order order = getOrderById(orderId);

        // Delete existing items
        for (OrderItem item : new ArrayList<>(order.getOrderItems())) {
            orderItemRepository.delete(item);
        }
        order.getOrderItems().clear();

        // Add new/updated items
        if (updatedItems != null) {
            for (OrderItem item : updatedItems) {
                Product product = productService.findById(item.getProduct().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found"));

                item.setProduct(product);
                item.setUnitPrice(product.getPrice());
                item.setOrder(order);

                orderItemRepository.save(item);
                order.addOrderItem(item);
            }
        }

        order.calculateTotalAmount();
        return saveOrder(order);
    }

    // ===================== PROCESS ORDERS =====================
    @Transactional
    public Order processOrder(Long orderId) {
        Order order = getOrderById(orderId);

        if (order.getType() == Order.OrderType.SALE) {
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

        if (order.getStatus() != Order.OrderStatus.PENDING && order.getType() == Order.OrderType.SALE) {
            for (OrderItem item : order.getOrderItems()) {
                productService.increaseStock(item.getProduct().getId(), item.getQuantity());
            }
        }

        order.cancel();
        return saveOrder(order);
    }

    // ===================== SEARCH =====================
    public List<Order> searchOrders(String searchTerm) {
        return findAll().stream()
                .filter(order ->
                        order.getOrderNumber().toLowerCase().contains(searchTerm.toLowerCase()) ||
                        (order.getSupplier() != null &&
                         order.getSupplier().getName().toLowerCase().contains(searchTerm.toLowerCase()))
                )
                .collect(Collectors.toList());
    }

    // ===================== ALERTS =====================
    public List<String> generateOrderAlerts() {
        List<String> alerts = new ArrayList<>();

        long pendingCount = getPendingOrders().size();
        if (pendingCount > 0) {
            alerts.add(String.format("PENDING ORDERS: %d orders awaiting processing", pendingCount));
        }

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

    public List<Order> getRecentOrders() {
        return findAll().stream()
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }
}
