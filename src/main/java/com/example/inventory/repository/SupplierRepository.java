package com.example.inventory.repository;

import com.example.inventory.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    
    // Find supplier by name
    Optional<Supplier> findByName(String name);
    
    // Find supplier by email
    Optional<Supplier> findByEmail(String email);
    
    // Find suppliers by status
    List<Supplier> findByStatus(Supplier.SupplierStatus status);
    
    // Find active suppliers
    @Query("SELECT s FROM Supplier s WHERE s.status = 'ACTIVE'")
    List<Supplier> findActiveSuppliers();
    
    // Find suppliers by name containing (case insensitive)
    List<Supplier> findByNameContainingIgnoreCase(String name);
    
    // Find suppliers with products
    @Query("SELECT DISTINCT s FROM Supplier s JOIN s.products p")
    List<Supplier> findSuppliersWithProducts();
    
    // Count products by supplier
    @Query("SELECT COUNT(p) FROM Product p WHERE p.supplier.id = :supplierId")
    Long countProductsBySupplier(@Param("supplierId") Long supplierId);
    
    // Find suppliers with pending orders
    @Query("SELECT DISTINCT s FROM Supplier s JOIN s.orders o WHERE o.status = 'PENDING'")
    List<Supplier> findSuppliersWithPendingOrders();
}
