package com.example.inventory.controller;

import com.example.inventory.model.User;
import com.example.inventory.security.JwtService;
import com.example.inventory.service.UserService;

// import io.jsonwebtoken.Claims;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        // Check if username or email already exists
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "Username already taken"));
        }
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "Email already registered"));
        }

        User newUser = userService.register(user);
        return ResponseEntity.ok(Map.of(
            "message", "User registered successfully",
            "user", newUser.getUsername()
        ));
    }

       @PostMapping("/login")
            public ResponseEntity<?> login(@RequestBody User user) {
             return userService.findByUsername(user.getUsername())
            .filter(u -> userService.checkPassword(user.getPassword(), u.getPassword()))
           .map(u -> {
                String token = jwtService.generateToken(u.getUsername(), u.getEmail());
                return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "user", u.getUsername(),
                    "email", u.getEmail(),
                    "token", token
                ));
            })

            .orElse(ResponseEntity.status(401).body(Map.of("error", "Invalid username or password")));
}

@GetMapping("/me")
public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        return ResponseEntity.status(401).body(Map.of("error", "Missing or invalid Authorization header"));
    }

    String token = authHeader.substring(7);
    // String token = authHeader.substring(7);
    // System.out.println("Incoming JWT: " + token);
    // Claims claims = jwtService.extractAllClaims(token);
    // System.out.println("Extracted claims: " + claims);

    try {
        if (!jwtService.validateToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
        }

        String username = jwtService.extractUsername(token);
        String email = jwtService.extractEmail(token);

        return ResponseEntity.ok(Map.of(
                "username", username,
                "email", email !=null ? email : "N/A"
        ));
    } catch (Exception e) {
        return ResponseEntity.status(500).body(Map.of("error", "Failed to parse JWT: " + e.getMessage()));
    }
}



}
