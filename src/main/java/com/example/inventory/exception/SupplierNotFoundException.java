package com.example.inventory.exception;

public class SupplierNotFoundException extends RuntimeException {
    public SupplierNotFoundException(String message) {
        super(message);
    }
    
    public SupplierNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
