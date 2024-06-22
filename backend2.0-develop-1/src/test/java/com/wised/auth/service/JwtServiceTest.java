package com.wised.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        jwtService.setSecretKey("Ma7xVrdhbEYNiFUjoiWdDZtxdZdfgsdfKH80id2Tq1bZC64");
        jwtService.setJwtExpiration(600000); // 10 minutes
        jwtService.setResetTokenExpiration(300000); // 5 minutes
        jwtService.setRefreshExpiration(1200000); // 20 minutes
    }

    @Test
    public void testExtractUsername() {
        String token = createTestToken("testUser");
        when(userDetails.getUsername()).thenReturn("testUser");

        String username = jwtService.extractUsername(token);

        assertEquals("testUser", username);
    }

    @Test
    public void testGenerateToken() {
        when(userDetails.getUsername()).thenReturn("testUser");

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertEquals("testUser", jwtService.extractUsername(token));
    }


    @Test
    public void testGenerateRefreshToken() {
        when(userDetails.getUsername()).thenReturn("testUser");

        String token = jwtService.generateRefreshToken(userDetails);

        assertNotNull(token);
        assertEquals("testUser", jwtService.extractUsername(token));
    }

    @Test
    public void testGenerateResetToken() {
        when(userDetails.getUsername()).thenReturn("testUser");

        String token = jwtService.generateResetToken(userDetails);

        assertNotNull(token);
        assertEquals("testUser", jwtService.extractUsername(token));
    }

    @Test
    public void testIsTokenValid() {
        String token = createTestToken("testUser");
        when(userDetails.getUsername()).thenReturn("testUser");

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    public void testIsTokenExpired() {
        String token = createExpiredToken("testUser");

        boolean isExpired = jwtService.isTokenExpired(token);

        assertTrue(isExpired);
    }

    @Test
    public void testIsTokenExpired_NotExpired() {
        String token = createTestToken("testUser");

        boolean isExpired = jwtService.isTokenExpired(token);

        assertFalse(isExpired);
    }

    @Test
    public void testExtractAllClaims() {
        String token = createTestToken("testUser");

        Claims claims = jwtService.extractAllClaims(token);

        assertNotNull(claims);
        assertEquals("testUser", claims.getSubject());
    }

    private String createTestToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 600000)) // 10 minutes
                .signWith(jwtService.getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String createExpiredToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis() - 600000)) // 10 minutes ago
                .setExpiration(new Date(System.currentTimeMillis() - 300000)) // 5 minutes ago
                .signWith(jwtService.getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}

