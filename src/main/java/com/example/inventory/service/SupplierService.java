package com.example.inventory.service;

import com.example.inventory.exception.SupplierNotFoundException;
import com.example.inventory.model.Supplier;
import com.example.inventory.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupplierService {
    
    private final SupplierRepository supplierRepository;
    
    @Autowired
    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }
    
    // CRUD Operations
    public Supplier saveSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }
    
    public Optional<Supplier> findById(Long id) {
        return supplierRepository.findById(id);
    }
    
    public Supplier getSupplierById(Long id) {
        return findById(id)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found with ID: " + id));
    }
    
    public Optional<Supplier> findByName(String name) {
        return supplierRepository.findByName(name);
    }
    
    public Optional<Supplier> findByEmail(String email) {
        return supplierRepository.findByEmail(email);
    }
    
    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }
    
    public void deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new SupplierNotFoundException("Supplier not found with ID: " + id);
        }
        supplierRepository.deleteById(id);
    }
    
    // Functional Programming: Stream operations for filtering and analytics
    
    // Filter suppliers by status
    public List<Supplier> getSuppliersByStatus(Supplier.SupplierStatus status) {
        return findAll().stream()
                .filter(supplier -> supplier.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    // Get active suppliers only
    public List<Supplier> getActiveSuppliers() {
        return findAll().stream()
                .filter(Supplier::isActive)
                .collect(Collectors.toList());
    }
    
    // Filter suppliers by custom criteria
    public List<Supplier> filterSuppliers(Predicate<Supplier> criteria) {
        return findAll().stream()
                .filter(criteria)
                .collect(Collectors.toList());
    }
    
    // Map suppliers to specific attributes
    public <T> List<T> mapSuppliers(Function<Supplier, T> mapper) {
        return findAll().stream()
                .map(mapper)
                .collect(Collectors.toList());
    }
    
    // Analytics: Aggregate operations
    
    // Count suppliers by status
    public Map<Supplier.SupplierStatus, Long> getSupplierCountByStatus() {
        return findAll().stream()
                .collect(Collectors.groupingBy(
                        Supplier::getStatus,
                        Collectors.counting()
                ));
    }
    
    // Group suppliers by location (city from address)
    public Map<String, List<Supplier>> groupSuppliersByLocation() {
        return findAll().stream()
                .filter(supplier -> supplier.getAddress() != null && !supplier.getAddress().trim().isEmpty())
                .collect(Collectors.groupingBy(supplier -> {
                    // Extract city from address (simplified - assumes last part is city)
                    String[] addressParts = supplier.getAddress().split(",");
                    return addressParts.length > 0 ? 
                           addressParts[addressParts.length - 1].trim() : 
                           "Unknown";
                }));
    }
    
    // Get supplier names only
    public List<String> getSupplierNames() {
        return findAll().stream()
                .map(Supplier::getName)
                .sorted()
                .collect(Collectors.toList());
    }
    
    // Business Logic with Exception Handling
    
    @Transactional
    public Supplier activateSupplier(Long id) {
        Supplier supplier = getSupplierById(id);
        supplier.activate();
        return supplierRepository.save(supplier);
    }
    
    @Transactional
    public Supplier deactivateSupplier(Long id) {
        Supplier supplier = getSupplierById(id);
        supplier.deactivate();
        return supplierRepository.save(supplier);
    }
    
    @Transactional
    public Supplier suspendSupplier(Long id) {
        Supplier supplier = getSupplierById(id);
        supplier.suspend();
        return supplierRepository.save(supplier);
    }
    
    // Search functionality
    public List<Supplier> searchSuppliers(String searchTerm) {
        return findAll().stream()
                .filter(supplier -> 
                    supplier.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    (supplier.getEmail() != null && supplier.getEmail().toLowerCase().contains(searchTerm.toLowerCase())) ||
                    (supplier.getContactPerson() != null && supplier.getContactPerson().toLowerCase().contains(searchTerm.toLowerCase())) ||
                    (supplier.getAddress() != null && supplier.getAddress().toLowerCase().contains(searchTerm.toLowerCase()))
                )
                .collect(Collectors.toList());
    }
    
    // Validation
    public boolean isSupplierNameUnique(String name, Long excludeId) {
        return findAll().stream()
                .filter(supplier -> !supplier.getId().equals(excludeId))
                .noneMatch(supplier -> supplier.getName().equalsIgnoreCase(name));
    }
    
    public boolean isSupplierEmailUnique(String email, Long excludeId) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Email is optional
        }
        return findAll().stream()
                .filter(supplier -> !supplier.getId().equals(excludeId))
                .filter(supplier -> supplier.getEmail() != null)
                .noneMatch(supplier -> supplier.getEmail().equalsIgnoreCase(email));
    }
    
    // Supplier performance analysis
    public List<Supplier> getReliableSuppliers() {
        // Suppliers that are active and have products
        return findAll().stream()
                .filter(Supplier::isActive)
                .filter(supplier -> !supplier.getProducts().isEmpty())
                .collect(Collectors.toList());
    }
    
    // Alert generation for supplier issues
    public List<String> generateSupplierAlerts() {
        return findAll().stream()
                .filter(supplier -> supplier.getStatus() != Supplier.SupplierStatus.ACTIVE)
                .map(supplier -> String.format(
                    "SUPPLIER ALERT: %s is %s - Contact: %s",
                    supplier.getName(),
                    supplier.getStatus().toString().toLowerCase(),
                    supplier.getEmail() != null ? supplier.getEmail() : "No email"
                ))
                .collect(Collectors.toList());
    }
}
