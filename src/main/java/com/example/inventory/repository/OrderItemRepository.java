package com.example.inventory.repository;

import com.example.inventory.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    List<OrderItem> findByOrderId(Long orderId);
    
    List<OrderItem> findByProductId(Long productId);
    
    List<OrderItem> findByOrderIdAndProductId(Long orderId, Long productId);
    
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product.id = :productId")
    Long getTotalQuantityOrderedForProduct(@Param("productId") Long productId);
    
    @Query("SELECT SUM(oi.quantity * oi.unitPrice) FROM OrderItem oi WHERE oi.order.id = :orderId")
    BigDecimal getTotalValueForOrder(@Param("orderId") Long orderId);
    
    @Query("SELECT oi.product.id, SUM(oi.quantity) as totalQuantity FROM OrderItem oi GROUP BY oi.product.id ORDER BY totalQuantity DESC")
    List<Object[]> findMostOrderedProducts();
}
