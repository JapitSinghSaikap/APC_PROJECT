package com.example.inventory.config;
import com.example.inventory.model.Product;
import com.example.inventory.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                Product laptop = new Product();
                laptop.setName("Laptop");
                laptop.setSku("LAP-001");
                laptop.setDescription("High performance laptop");
                laptop.setStockQuantity(50);
                laptop.setMinStockLevel(10);
                laptop.setPrice(new BigDecimal("1200.00"));
                laptop.setCategory("Electronics");

                Product desk = new Product();
                desk.setName("Office Desk");
                desk.setSku("DSK-001");
                desk.setDescription("Wooden office desk");
                desk.setStockQuantity(20);
                desk.setMinStockLevel(5);
                desk.setPrice(new BigDecimal("350.00"));
                desk.setCategory("Furniture");

                Product tShirt = new Product();
                tShirt.setName("T-Shirt");
                tShirt.setSku("TSH-001");
                tShirt.setDescription("Cotton T-Shirt");
                tShirt.setStockQuantity(200);
                tShirt.setMinStockLevel(50);
                tShirt.setPrice(new BigDecimal("19.99"));
                tShirt.setCategory("Apparel");

                productRepository.save(laptop);
                productRepository.save(desk);
                productRepository.save(tShirt);

                System.out.println("✅ Sample products added to database");
            }
        };
    }
}
