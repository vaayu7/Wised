package com.wised.auth.controller;

import com.wised.auth.dtos.AuthenticationRequest;
import com.wised.auth.dtos.AuthenticationResponse;
import com.wised.auth.dtos.RegisterRequest;
import com.wised.auth.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


/**
 * The `AuthenticationController` class handles user authentication and registration.
 *
 * This controller provides endpoints for user registration, authentication, and token refresh.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    /**
     * Register a new user.
     *
     * @param request The registration request containing user details.
     * @return A response entity with authentication details (HTTP 200 OK) if registration is successful,
     *         or an error response (HTTP 401 Unauthorized) if registration fails.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ){
        AuthenticationResponse response = service.register(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response); // Successful authentication, HTTP 200 OK
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // Authentication error, HTTP 401 Unauthorized
        }
    }


    /**
     * Authenticate a user.
     *
     * @param request The authentication request containing user credentials.
     * @return A response entity with authentication details (HTTP 200 OK) if authentication is successful,
     *         or an error response (HTTP 401 Unauthorized) if authentication fails.
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ){
        AuthenticationResponse response = service.authenticate(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response); // Successful authentication, HTTP 200 OK
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // Authentication error, HTTP 401 Unauthorized
        }
    }

    /**
     * Refresh an authentication token.
     *
     * @param request The HTTP request for token refresh.
     * @return A response entity with a refreshed authentication token (HTTP 200 OK) if refresh is successful,
     *         or an error response (HTTP 401 Unauthorized) if refresh fails.
     * @throws IOException If an I/O error occurs during token refresh.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            HttpServletRequest request
    ) throws IOException {

        AuthenticationResponse response = service.refreshToken(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response); // Successful token refresh, HTTP 200 OK
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // Token refresh error, HTTP 401 Unauthorized
        }
    }
}
