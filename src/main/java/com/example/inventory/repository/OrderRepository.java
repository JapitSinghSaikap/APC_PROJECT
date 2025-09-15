package com.example.inventory.repository;

import com.example.inventory.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find order by order number
    Optional<Order> findByOrderNumber(String orderNumber);
    
    // Find orders by status
    List<Order> findByStatus(Order.OrderStatus status);
    
    // Find orders by type
    List<Order> findByType(Order.OrderType type);
    
    // Find orders by supplier
    List<Order> findBySupplierId(Long supplierId);
    
    // Find pending orders
    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING'")
    List<Order> findPendingOrders();
    
    // Find delayed orders (expected delivery date passed but not delivered)
    @Query("SELECT o FROM Order o WHERE o.expectedDeliveryDate < :currentDate AND o.actualDeliveryDate IS NULL")
    List<Order> findDelayedOrders(@Param("currentDate") LocalDateTime currentDate);
    
    // Find orders within date range
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    // Calculate total revenue within date range
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate AND o.status = 'DELIVERED'")
    BigDecimal calculateTotalRevenue(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
    
    // Find recent orders (last N orders)
    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC")
    List<Order> findRecentOrders();
    
    // Count orders by status
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countOrdersByStatus(@Param("status") Order.OrderStatus status);
    
    // Find orders by supplier and status
    List<Order> findBySupplierIdAndStatus(Long supplierId, Order.OrderStatus status);
}
