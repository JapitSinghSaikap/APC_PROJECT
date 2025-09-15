package com.example.inventory.config;

import com.example.inventory.model.*;
import com.example.inventory.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private WarehouseRepository warehouseRepository;
    
    @Autowired
    private SupplierRepository supplierRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create sample warehouses
        Warehouse mainWarehouse = new Warehouse();
        mainWarehouse.setName("Main Warehouse");
        mainWarehouse.setLocation("New York, NY");
        
        Warehouse northWarehouse = new Warehouse();
        northWarehouse.setName("North Distribution Center");
        northWarehouse.setLocation("Boston, MA");
        
        Warehouse southWarehouse = new Warehouse();
        southWarehouse.setName("South Distribution Center");
        southWarehouse.setLocation("Atlanta, GA");
        
        List<Warehouse> warehouses = warehouseRepository.saveAll(Arrays.asList(
            mainWarehouse, northWarehouse, southWarehouse
        ));
        
        // Create sample suppliers
        Supplier techSupplier = new Supplier();
        techSupplier.setName("TechCorp Electronics");
        techSupplier.setContactPerson("John Smith");
        techSupplier.setEmail("john@techcorp.com");
        techSupplier.setPhone("+1-555-0123");
        techSupplier.setAddress("123 Tech Street, Silicon Valley, CA");
        techSupplier.setStatus(Supplier.SupplierStatus.ACTIVE);
        
        Supplier furnitureSupplier = new Supplier();
        furnitureSupplier.setName("Quality Furniture Co.");
        furnitureSupplier.setContactPerson("Sarah Johnson");
        furnitureSupplier.setEmail("sarah@qualityfurniture.com");
        furnitureSupplier.setPhone("+1-555-0456");
        furnitureSupplier.setAddress("456 Furniture Ave, Grand Rapids, MI");
        furnitureSupplier.setStatus(Supplier.SupplierStatus.ACTIVE);
        
        Supplier clothingSupplier = new Supplier();
        clothingSupplier.setName("Fashion Forward Inc.");
        clothingSupplier.setContactPerson("Mike Wilson");
        clothingSupplier.setEmail("mike@fashionforward.com");
        clothingSupplier.setPhone("+1-555-0789");
        clothingSupplier.setAddress("789 Fashion Blvd, Los Angeles, CA");
        clothingSupplier.setStatus(Supplier.SupplierStatus.ACTIVE);
        
        Supplier inactiveSupplier = new Supplier();
        inactiveSupplier.setName("Old Electronics Ltd.");
        inactiveSupplier.setContactPerson("Bob Brown");
        inactiveSupplier.setEmail("bob@oldelectronics.com");
        inactiveSupplier.setPhone("+1-555-0999");
        inactiveSupplier.setAddress("999 Old Tech Road, Detroit, MI");
        inactiveSupplier.setStatus(Supplier.SupplierStatus.INACTIVE);
        
        List<Supplier> suppliers = supplierRepository.saveAll(Arrays.asList(
            techSupplier, furnitureSupplier, clothingSupplier, inactiveSupplier
        ));
        
        // Create sample products
        Product laptop = new Product();
        laptop.setSku("TECH-001");
        laptop.setName("Gaming Laptop Pro");
        laptop.setDescription("High-performance gaming laptop with RTX graphics");
        laptop.setCategory("Electronics");
        laptop.setPrice(new BigDecimal("1299.99"));
        laptop.setStockQuantity(25);
        laptop.setMinStockLevel(5);
        laptop.setWarehouse(mainWarehouse);
        laptop.setSupplier(techSupplier);
        
        Product smartphone = new Product();
        smartphone.setSku("TECH-002");
        smartphone.setName("Smartphone X1");
        smartphone.setDescription("Latest model smartphone with advanced camera");
        smartphone.setCategory("Electronics");
        smartphone.setPrice(new BigDecimal("899.99"));
        smartphone.setStockQuantity(50);
        smartphone.setMinStockLevel(10);
        smartphone.setWarehouse(mainWarehouse);
        smartphone.setSupplier(techSupplier);
        
        Product tablet = new Product();
        tablet.setSku("TECH-003");
        tablet.setName("Pro Tablet 12");
        tablet.setDescription("Professional tablet for creative work");
        tablet.setCategory("Electronics");
        tablet.setPrice(new BigDecimal("599.99"));
        tablet.setStockQuantity(15);
        tablet.setMinStockLevel(3);
        tablet.setWarehouse(northWarehouse);
        tablet.setSupplier(techSupplier);
        
        Product officeChair = new Product();
        officeChair.setSku("FURN-001");
        officeChair.setName("Ergonomic Office Chair");
        officeChair.setDescription("Comfortable ergonomic chair for long work sessions");
        officeChair.setCategory("Furniture");
        officeChair.setPrice(new BigDecimal("299.99"));
        officeChair.setStockQuantity(30);
        officeChair.setMinStockLevel(5);
        officeChair.setWarehouse(southWarehouse);
        officeChair.setSupplier(furnitureSupplier);
        
        Product desk = new Product();
        desk.setSku("FURN-002");
        desk.setName("Standing Desk Pro");
        desk.setDescription("Adjustable height standing desk");
        desk.setCategory("Furniture");
        desk.setPrice(new BigDecimal("449.99"));
        desk.setStockQuantity(20);
        desk.setMinStockLevel(3);
        desk.setWarehouse(southWarehouse);
        desk.setSupplier(furnitureSupplier);
        
        Product tshirt = new Product();
        tshirt.setSku("CLOTH-001");
        tshirt.setName("Premium Cotton T-Shirt");
        tshirt.setDescription("High-quality cotton t-shirt in various colors");
        tshirt.setCategory("Clothing");
        tshirt.setPrice(new BigDecimal("29.99"));
        tshirt.setStockQuantity(100);
        tshirt.setMinStockLevel(20);
        tshirt.setWarehouse(mainWarehouse);
        tshirt.setSupplier(clothingSupplier);
        
        Product jeans = new Product();
        jeans.setSku("CLOTH-002");
        jeans.setName("Designer Jeans");
        jeans.setDescription("Premium denim jeans with modern fit");
        jeans.setCategory("Clothing");
        jeans.setPrice(new BigDecimal("79.99"));
        jeans.setStockQuantity(60);
        jeans.setMinStockLevel(15);
        jeans.setWarehouse(northWarehouse);
        jeans.setSupplier(clothingSupplier);
        
        Product lowStockItem = new Product();
        lowStockItem.setSku("TECH-004");
        lowStockItem.setName("Wireless Headphones");
        lowStockItem.setDescription("Premium wireless noise-canceling headphones");
        lowStockItem.setCategory("Electronics");
        lowStockItem.setPrice(new BigDecimal("199.99"));
        lowStockItem.setStockQuantity(2);
        lowStockItem.setMinStockLevel(10);
        lowStockItem.setWarehouse(mainWarehouse);
        lowStockItem.setSupplier(techSupplier);
        
        List<Product> products = productRepository.saveAll(Arrays.asList(
            laptop, smartphone, tablet, officeChair, desk, tshirt, jeans, lowStockItem
        ));
        
        // Create sample orders
        Order purchaseOrder1 = new Order();
        purchaseOrder1.setOrderNumber("PO-2025-001");
        purchaseOrder1.setType(Order.OrderType.PURCHASE);
        purchaseOrder1.setStatus(Order.OrderStatus.CONFIRMED);
        purchaseOrder1.setOrderDate(LocalDateTime.now().minusDays(5));
        purchaseOrder1.setExpectedDeliveryDate(LocalDateTime.now().plusDays(5));
        purchaseOrder1.setSupplier(techSupplier);
        
        Order purchaseOrder2 = new Order();
        purchaseOrder2.setOrderNumber("PO-2025-002");
        purchaseOrder2.setType(Order.OrderType.PURCHASE);
        purchaseOrder2.setStatus(Order.OrderStatus.SHIPPED);
        purchaseOrder2.setOrderDate(LocalDateTime.now().minusDays(3));
        purchaseOrder2.setExpectedDeliveryDate(LocalDateTime.now().plusDays(2));
        purchaseOrder2.setSupplier(furnitureSupplier);
        
        Order saleOrder1 = new Order();
        saleOrder1.setOrderNumber("SO-2025-001");
        saleOrder1.setType(Order.OrderType.SALE);
        saleOrder1.setStatus(Order.OrderStatus.DELIVERED);
        saleOrder1.setOrderDate(LocalDateTime.now().minusDays(7));
        saleOrder1.setExpectedDeliveryDate(LocalDateTime.now().minusDays(2));
        saleOrder1.setActualDeliveryDate(LocalDateTime.now().minusDays(1));
        
        Order pendingOrder = new Order();
        pendingOrder.setOrderNumber("PO-2025-003");
        pendingOrder.setType(Order.OrderType.PURCHASE);
        pendingOrder.setStatus(Order.OrderStatus.PENDING);
        pendingOrder.setOrderDate(LocalDateTime.now());
        pendingOrder.setExpectedDeliveryDate(LocalDateTime.now().plusDays(10));
        pendingOrder.setSupplier(clothingSupplier);
        
        List<Order> orders = orderRepository.saveAll(Arrays.asList(
            purchaseOrder1, purchaseOrder2, saleOrder1, pendingOrder
        ));
        
        // Create sample order items
        OrderItem item1 = new OrderItem();
        item1.setOrder(purchaseOrder1);
        item1.setProduct(laptop);
        item1.setQuantity(10);
        item1.setUnitPrice(new BigDecimal("1199.99"));
        
        OrderItem item2 = new OrderItem();
        item2.setOrder(purchaseOrder1);
        item2.setProduct(smartphone);
        item2.setQuantity(20);
        item2.setUnitPrice(new BigDecimal("799.99"));
        
        OrderItem item3 = new OrderItem();
        item3.setOrder(purchaseOrder2);
        item3.setProduct(officeChair);
        item3.setQuantity(15);
        item3.setUnitPrice(new BigDecimal("279.99"));
        
        OrderItem item4 = new OrderItem();
        item4.setOrder(purchaseOrder2);
        item4.setProduct(desk);
        item4.setQuantity(10);
        item4.setUnitPrice(new BigDecimal("419.99"));
        
        OrderItem item5 = new OrderItem();
        item5.setOrder(saleOrder1);
        item5.setProduct(tshirt);
        item5.setQuantity(50);
        item5.setUnitPrice(new BigDecimal("29.99"));
        
        OrderItem item6 = new OrderItem();
        item6.setOrder(saleOrder1);
        item6.setProduct(jeans);
        item6.setQuantity(25);
        item6.setUnitPrice(new BigDecimal("79.99"));
        
        OrderItem item7 = new OrderItem();
        item7.setOrder(pendingOrder);
        item7.setProduct(lowStockItem);
        item7.setQuantity(50);
        item7.setUnitPrice(new BigDecimal("189.99"));
        
        orderItemRepository.saveAll(Arrays.asList(
            item1, item2, item3, item4, item5, item6, item7
        ));
        
        // Update order totals
        purchaseOrder1.calculateTotalAmount();
        purchaseOrder2.calculateTotalAmount();
        saleOrder1.calculateTotalAmount();
        pendingOrder.calculateTotalAmount();
        
        orderRepository.saveAll(Arrays.asList(
            purchaseOrder1, purchaseOrder2, saleOrder1, pendingOrder
        ));
        
        System.out.println("Sample data initialized successfully!");
        System.out.println("Created: " + warehouses.size() + " warehouses");
        System.out.println("Created: " + suppliers.size() + " suppliers");
        System.out.println("Created: " + products.size() + " products");
        System.out.println("Created: " + orders.size() + " orders");
        System.out.println("Created: 7 order items");
    }
}
