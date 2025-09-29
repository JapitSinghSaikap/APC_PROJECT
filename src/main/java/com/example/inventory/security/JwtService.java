package com.example.inventory.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private static final String SECRET = "ThisIsASecretKeyForJwtTokenThatIsAtLeast32Chars!";
    private static final long EXPIRATION = 86400000; 
    private final Key secretKey;

    public JwtService() {
        this.secretKey = Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(String username, String email) {
        return Jwts.builder()
                .setSubject(username)
                .addClaims(Map.of("email", email))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token); 
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false; 
        }
    }

    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            return null; 
        }
    }

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims != null ? claims.get("email", String.class) : null;
    }

    public String validateTokenAndGetUsername(String token) {
        if (validateToken(token)) {
            return extractUsername(token);
        }
        return null;
    }
}
