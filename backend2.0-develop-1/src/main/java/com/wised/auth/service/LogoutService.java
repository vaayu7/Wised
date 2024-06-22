package com.wised.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wised.auth.dtos.CustomLogoutResponse;
import com.wised.auth.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Service class for handling user logout.
 */
@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private  final  JwtService jwtService;
    private  final TokenRepository tokenRepository;

    /**
     * Handle user logout by invalidating the JWT token.
     *
     * @param request        The HTTP request.
     * @param response       The HTTP response.
     * @param authentication The authentication object representing the user.
     */
    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {
        try {
            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String userEmail;

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                // If the Authorization header is missing or doesn't start with "Bearer ",
                // continue processing the request without authentication.
                return;
            }

            jwt = authHeader.substring(7); // Remove "Bearer " prefix

            var storedToken = tokenRepository.findByToken(jwt)
                    .orElse(null);

            userEmail = jwtService.extractUsername(jwt);

            if (storedToken != null) {
                storedToken.setExpired(true);
                storedToken.setRevoked(true);
                tokenRepository.save(storedToken);
            }

            // Create a custom logout response
            CustomLogoutResponse logoutResponse = CustomLogoutResponse.builder()
                    .success(true)
                    .message("Successfully logged out")
                    .email(userEmail)
                    .build();

            // Serialize the response to JSON and write it to the response body
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                objectMapper.writeValue(response.getWriter(), logoutResponse);
                response.getWriter().flush();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        } catch (UsernameNotFoundException e) {
            // Handle exceptions and create a custom response for user not found
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json");

            // Create a custom response object
            CustomLogoutResponse logoutResponse = CustomLogoutResponse.builder()
                    .success(false)
                    .error(e.getMessage())
                    .message("User not found")
                    .build();

            // Serialize the response to JSON and write it to the response body
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                objectMapper.writeValue(response.getWriter(), logoutResponse);
                response.getWriter().flush();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        } catch (Exception e) {
            // Handle exceptions and create a custom response for other errors
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");

            // Create a custom response object
            CustomLogoutResponse logoutResponse = CustomLogoutResponse.builder()
                    .success(false)
                    .error(e.getMessage())
                    .message("An error occurred while logging out")
                    .build();

            // Serialize the response to JSON and write it to the response body
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                objectMapper.writeValue(response.getWriter(), logoutResponse);
                response.getWriter().flush();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
