package com.example.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.example.inventory.security.JwtService;
import com.example.inventory.service.UserService;

import static org.mockito.Mockito.mock;

@SpringBootTest
@Import({JwtService.class, UserService.class})
class InventoryApplicationTests {

    // Manually mock required beans
    private final JwtService jwtService = mock(JwtService.class);
    private final UserService userService = mock(UserService.class);

    @Test
    void contextLoads() {
        // Just ensures Spring context loads without errors
    }
}
