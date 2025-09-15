package com.example.inventory.controller;

import com.example.inventory.model.Supplier;
import com.example.inventory.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/suppliers")
@CrossOrigin(origins = "*")
public class SupplierController {
    
    private final SupplierService supplierService;
    
    @Autowired
    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }
    
    // GET /api/suppliers - Get all suppliers
    @GetMapping
    public ResponseEntity<List<Supplier>> getAllSuppliers() {
        List<Supplier> suppliers = supplierService.findAll();
        return ResponseEntity.ok(suppliers);
    }
    
    // GET /api/suppliers/{id} - Get supplier by ID
    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Long id) {
        Optional<Supplier> supplier = supplierService.findById(id);
        return supplier.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    // GET /api/suppliers/name/{name} - Get supplier by name
    @GetMapping("/name/{name}")
    public ResponseEntity<Supplier> getSupplierByName(@PathVariable String name) {
        Optional<Supplier> supplier = supplierService.findByName(name);
        return supplier.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    // POST /api/suppliers - Create new supplier
    @PostMapping
    public ResponseEntity<Supplier> createSupplier(@Valid @RequestBody Supplier supplier) {
        Supplier savedSupplier = supplierService.saveSupplier(supplier);
        return new ResponseEntity<>(savedSupplier, HttpStatus.CREATED);
    }
    
    // PUT /api/suppliers/{id} - Update supplier
    @PutMapping("/{id}")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id, @Valid @RequestBody Supplier supplier) {
        Optional<Supplier> existingSupplier = supplierService.findById(id);
        if (existingSupplier.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        supplier.setId(id);
        Supplier updatedSupplier = supplierService.saveSupplier(supplier);
        return ResponseEntity.ok(updatedSupplier);
    }
    
    // DELETE /api/suppliers/{id} - Delete supplier
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        try {
            supplierService.deleteSupplier(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // GET /api/suppliers/active - Get active suppliers
    @GetMapping("/active")
    public ResponseEntity<List<Supplier>> getActiveSuppliers() {
        List<Supplier> suppliers = supplierService.getActiveSuppliers();
        return ResponseEntity.ok(suppliers);
    }
    
    // GET /api/suppliers/status/{status} - Get suppliers by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Supplier>> getSuppliersByStatus(@PathVariable String status) {
        try {
            Supplier.SupplierStatus supplierStatus = Supplier.SupplierStatus.valueOf(status.toUpperCase());
            List<Supplier> suppliers = supplierService.getSuppliersByStatus(supplierStatus);
            return ResponseEntity.ok(suppliers);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // GET /api/suppliers/search - Search suppliers
    @GetMapping("/search")
    public ResponseEntity<List<Supplier>> searchSuppliers(@RequestParam String q) {
        List<Supplier> suppliers = supplierService.searchSuppliers(q);
        return ResponseEntity.ok(suppliers);
    }
    
    // PUT /api/suppliers/{id}/activate - Activate supplier
    @PutMapping("/{id}/activate")
    public ResponseEntity<Supplier> activateSupplier(@PathVariable Long id) {
        try {
            Supplier supplier = supplierService.activateSupplier(id);
            return ResponseEntity.ok(supplier);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // PUT /api/suppliers/{id}/deactivate - Deactivate supplier
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Supplier> deactivateSupplier(@PathVariable Long id) {
        try {
            Supplier supplier = supplierService.deactivateSupplier(id);
            return ResponseEntity.ok(supplier);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // PUT /api/suppliers/{id}/suspend - Suspend supplier
    @PutMapping("/{id}/suspend")
    public ResponseEntity<Supplier> suspendSupplier(@PathVariable Long id) {
        try {
            Supplier supplier = supplierService.suspendSupplier(id);
            return ResponseEntity.ok(supplier);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // GET /api/suppliers/analytics/count-by-status - Get supplier count by status
    @GetMapping("/analytics/count-by-status")
    public ResponseEntity<Map<Supplier.SupplierStatus, Long>> getSupplierCountByStatus() {
        Map<Supplier.SupplierStatus, Long> countByStatus = supplierService.getSupplierCountByStatus();
        return ResponseEntity.ok(countByStatus);
    }
    
    // GET /api/suppliers/analytics/grouped-by-location - Get suppliers grouped by location
    @GetMapping("/analytics/grouped-by-location")
    public ResponseEntity<Map<String, List<Supplier>>> getSuppliersGroupedByLocation() {
        Map<String, List<Supplier>> groupedSuppliers = supplierService.groupSuppliersByLocation();
        return ResponseEntity.ok(groupedSuppliers);
    }
    
    // GET /api/suppliers/names - Get supplier names only
    @GetMapping("/names")
    public ResponseEntity<List<String>> getSupplierNames() {
        List<String> names = supplierService.getSupplierNames();
        return ResponseEntity.ok(names);
    }
    
    // GET /api/suppliers/reliable - Get reliable suppliers
    @GetMapping("/reliable")
    public ResponseEntity<List<Supplier>> getReliableSuppliers() {
        List<Supplier> suppliers = supplierService.getReliableSuppliers();
        return ResponseEntity.ok(suppliers);
    }
    
    // GET /api/suppliers/alerts - Get supplier alerts
    @GetMapping("/alerts")
    public ResponseEntity<List<String>> getSupplierAlerts() {
        List<String> alerts = supplierService.generateSupplierAlerts();
        return ResponseEntity.ok(alerts);
    }
}
