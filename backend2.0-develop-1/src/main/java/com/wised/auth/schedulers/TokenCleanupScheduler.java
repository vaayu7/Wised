package com.wised.auth.schedulers;

import com.wised.auth.model.Token;
import com.wised.auth.repository.TokenRepository;
import com.wised.auth.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for cleaning up expired tokens from the database.
 */
@Service
@AllArgsConstructor
public class TokenCleanupScheduler {

    private TokenRepository tokenRepository; // Replace with your actual token repository
    private JwtService jwtService; // Inject your JWT service

    /**
     * Scheduled method to clean up expired tokens.
     * This method runs at a fixed rate to periodically check and remove expired tokens from the database.
     */
    @Scheduled(fixedRate = 600000) // Run every 10 minutes (adjust the rate as needed)
    public void cleanupExpiredTokens() {
        List<Token> tokens = tokenRepository.findAll(); // Retrieve all tokens from the database

        // Iterate through the tokens and check if they are expired
        for (Token token : tokens) {
            try {
                if (jwtService.isTokenExpired(token.getToken())) {
                    // Token is expired, remove it from the database
                    tokenRepository.delete(token);
                    System.out.println("Expired token found and removed: " + token.getId());
                }
            } catch (ExpiredJwtException ex) {
                // Handle the exception gracefully (log it, for example)
                System.out.println("Exception handling expired token: " + ex.getMessage());
            }
        }
    }
}

