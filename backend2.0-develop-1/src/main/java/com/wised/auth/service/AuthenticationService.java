package com.wised.auth.service;


import com.wised.auth.dtos.AuthenticationRequest;
import com.wised.auth.dtos.AuthenticationResponse;
import com.wised.auth.dtos.RegisterRequest;
import com.wised.auth.enums.TokenType;
import com.wised.auth.exception.InvalidCredentialsException;
import com.wised.auth.exception.UserNotFoundException;
import com.wised.auth.model.Token;
import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.TokenRepository;
import com.wised.auth.repository.UserRepository;
import com.wised.helpandsettings.service.DeactivateAndDeleteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Calendar;
import java.util.Optional;


/**
 * The `AuthenticationService` class handles user registration, authentication, and token refresh.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    @Value("${application.security.allowed-attempts.login}")
    private int allowedAttempts;

    private final UserRepository repository; // Repository for user data
    private final PasswordEncoder passwordEncoder; // Password encoder for hashing passwords
    private final JwtService jwtService; // JWT service for token generation and validation
    private final AuthenticationManager authenticationManager; // Authentication manager for user authentication
    private final TokenRepository tokenRepository;
    private  final  UserProfileService userProfileService;
    private final DeactivateAndDeleteService deactivateAndDeleteService;

    /**
     * Register a new user with the provided registration details.
     *
     * @param request The registration request containing user details.
     * @return An AuthenticationResponse containing a JWT token upon successful registration.
     */
    public AuthenticationResponse register(RegisterRequest request) {
        try {
            // Create a new user entity with the provided details
            var user = User.builder()
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .role(request.getRole())
                    .accountLocked(false)
                    .build();

            // Save the user to the repository
            var savedUser = repository.save(user);

            //creating profile entry for user
            UserProfile userProfile = userProfileService.createProfileForUser(savedUser);

            // Generate a JWT token for the registered user
            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);

            savedUserToken(savedUser, jwtToken);

            // Return the JWT token in an AuthenticationResponse
            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .success(true)
                    .message("jwt token successfully created")
                    .refreshToken(refreshToken)
                    .build();
        } catch (DataIntegrityViolationException e) {
            // Handle the unique constraint violation exception here
            // This exception is thrown when there's a duplicate email
            return AuthenticationResponse.builder()
                    .error(e.getMessage())
                    .success(false)
                    .message("This email is already registered. Please log in with this email or use a different one to create account")
                    .build();
        } catch (Exception e) {
            // Handle other exceptions here
            e.printStackTrace(); // You can replace this with proper logging
            return AuthenticationResponse.builder()
                    .error(e.getMessage())
                    .message("Registration failed. Please try again.")
                    .success(false)
                    .build();
        }
    }

    /**
     * Authenticate a user using the provided email and password.
     *
     * @param request The authentication request containing email and password.
     * @return An AuthenticationResponse containing a JWT token upon successful authentication.
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {


            // Retrieve the authenticated user from the repository
            var optionalUser = repository.findByEmail(request.getEmail());

            if (!optionalUser.isPresent()){
                throw new UserNotFoundException("User not found with email: " + request.getEmail());
            }

            User user = optionalUser.get();

            // Check if the account is locked
            if (user.getAccountLocked()) {
                // Account is already locked, return an error response
                return AuthenticationResponse.builder()
                        .error("Account is locked")
                        .message("Your account has been temporarily locked due to multiple unsuccessful login attempts")
                        .success(false)
                        .build();
            }

            // Attempt to authenticate the user using the provided email and password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );



            // Reset the failed attempt count since the login was successful
            user.setFailedAttempt(0);
            user.setLockTime(null);

            // Generate a JWT token for the authenticated user
            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);
            revokeAllUserTokens(user);

            savedUserToken(user, jwtToken);
            //UpdateUserAccountStatus();


            // Return the JWT token in an AuthenticationResponse
            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .success(true)
                    .message("JWT token successfully created")
                    .refreshToken(refreshToken)
                    .build();

        } catch (AuthenticationException e) {
            if (e instanceof UsernameNotFoundException) {
                // User not found
                return AuthenticationResponse.builder()
                        .error(e.getMessage())
                        .message("User not found with email: " + request.getEmail())
                        .success(false)
                        .build();
            } else {
                // Invalid email or password
                // Update the failed attempt count
                updateFailedAttempt(request.getEmail());
                return AuthenticationResponse.builder()
                        .error(e.getMessage())
                        .message("Invalid email or password")
                        .success(false)
                        .build();
            }
        } catch (Exception e) {
            return AuthenticationResponse.builder()
                    .error(e.getMessage())
                    .message("Internal server error")
                    .success(false)
                    .build();
        }
    }

    /**
     * Refresh the access token using the provided refresh token.
     *
     * @param request The HTTP request containing the refresh token.
     * @return An AuthenticationResponse containing a new access token upon successful refresh.
     */
    public AuthenticationResponse refreshToken(HttpServletRequest request) throws IOException {
        try {
            // Extract the refresh token from the request
            final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            final String refreshToken;
            final String userEmail;

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                // If the Authorization header is missing or doesn't start with "Bearer ",
                // continue processing the request without authentication.
                throw new InvalidCredentialsException("Invalid token");
            }

            refreshToken = authHeader.substring(7); // Remove "Bearer " prefix
            userEmail = jwtService.extractUsername(refreshToken);

            if (userEmail == null) {
                // If the token doesn't contain a valid user email, return an error response.
                throw new UserNotFoundException("User not found");
            }

            // Attempt to load user details
            Optional<User> userDetailsOptional;
            User userDetails;

            userDetailsOptional = this.repository.findByEmail(userEmail);
            if (!userDetailsOptional.isPresent()) {
                // User not found in the repository
                throw new UserNotFoundException("User not found");
            }
            userDetails = userDetailsOptional.get();

            if (!jwtService.isTokenValid(refreshToken, userDetails)) {
                // If the token is not valid, return an error response.
                throw new InvalidCredentialsException("Invalid token");
            }

            // If everything is valid, generate a new access token and update the user's tokens
            var accessToken = jwtService.generateToken(userDetails);
            revokeAllUserTokens(userDetails);
            savedUserToken(userDetails, accessToken);

            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .success(true)
                    .message("successfully created new accessToken")
                    .refreshToken(refreshToken)
                    .build();

        } catch (InvalidCredentialsException e) {
            return AuthenticationResponse.builder()
                    .error(e.getMessage())
                    .success(false)
                    .message("invalid refresh token; please login again")
                    .build();
        } catch (UsernameNotFoundException e) {
            // If the user is not found, return an error response.
            return AuthenticationResponse.builder()
                    .error(e.getMessage())
                    .success(false)
                    .message("User not found")
                    .build();
        } catch (Exception e) {
            return AuthenticationResponse.builder()
                    .error(e.getMessage())
                    .success(false)
                    .message("An internal server error occurred.")
                    .build();
        }
    }

    /**
     * Revoke all tokens associated with a user.
     *
     * @param user The user for whom tokens need to be revoked.
     */
    public void revokeAllUserTokens(User user) {
        var validUserToken = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserToken.isEmpty()) {
            return;
        }
        validUserToken.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });
        tokenRepository.saveAll(validUserToken);
    }

    /**
     * Save a new token for a user.
     *
     * @param user     The user for whom the token is saved.
     * @param jwtToken The JWT token to be saved.
     */
    private void savedUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }


    /**
     * Update the number of failed login attempts for a user and potentially lock the account
     * if the number of failed attempts exceeds a threshold.
     *
     * @param email The email of the user whose failed attempts need to be updated.
     */
    private void updateFailedAttempt(String email) {
            var optionalUser = repository.findByEmail(email);



            User user = optionalUser.get();

            int failedAttempts = user.getFailedAttempt() + 1;
            user.setFailedAttempt(failedAttempts);

        // Check if the number of failed attempts exceeds the allowed threshold
            if (failedAttempts >= allowedAttempts) {
                // Lock the account for a specified duration
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MINUTE, 2); // Lock for a specific number of minutes
                // Alternatively, if you want to lock for a number of days:
                // calendar.add(Calendar.DAY_OF_YEAR, lockDurationDays); // Lock for a specific number of days

                user.setLockTime(calendar.getTime());
                user.setAccountLocked(true);
            }

            repository.save(user);

    }
}