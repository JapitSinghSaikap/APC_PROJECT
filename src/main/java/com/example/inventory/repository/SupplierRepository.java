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
    
    Optional<Supplier> findByName(String name);
    
    Optional<Supplier> findByEmail(String email);
    
    List<Supplier> findByStatus(Supplier.SupplierStatus status);
    
    @Query("SELECT s FROM Supplier s WHERE s.status = 'ACTIVE'")
    List<Supplier> findActiveSuppliers();
    
    List<Supplier> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT DISTINCT s FROM Supplier s JOIN s.products p")
    List<Supplier> findSuppliersWithProducts();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.supplier.id = :supplierId")
    Long countProductsBySupplier(@Param("supplierId") Long supplierId);
    
    @Query("SELECT DISTINCT s FROM Supplier s JOIN s.orders o WHERE o.status = 'PENDING'")
    List<Supplier> findSuppliersWithPendingOrders();
}
