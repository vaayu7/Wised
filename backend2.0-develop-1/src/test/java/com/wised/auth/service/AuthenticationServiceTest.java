package com.wised.auth.service;

import com.wised.auth.model.Token;
import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.TokenRepository;
import com.wised.auth.repository.UserRepository;
import com.wised.auth.dtos.AuthenticationRequest;
import com.wised.auth.dtos.AuthenticationResponse;
import com.wised.auth.dtos.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void registerTest() {
        // Prepare test data
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFullName("John Doe");
        registerRequest.setEmail("john.doe@example.com");
        registerRequest.setPassword("password");


        User savedUser = User.builder()
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .passwordHash("hashedPassword")
                .role(registerRequest.getRole())
                .accountLocked(false)
                .build();

        AuthenticationResponse expectedResponse = AuthenticationResponse.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .success(true)
                .message("jwt token successfully created")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");

        // Mocking UserProfileService behavior
        when(userProfileService.createProfileForUser(savedUser)).thenReturn(new UserProfile());

        // Test register method
        AuthenticationResponse actualResponse = authenticationService.register(registerRequest);

        // Verify
        assertNotNull(actualResponse);
        assertEquals(expectedResponse.isSuccess(), actualResponse.isSuccess());
        assertEquals(expectedResponse.getMessage(), actualResponse.getMessage());
        assertEquals(expectedResponse.getAccessToken(), actualResponse.getAccessToken());
        assertEquals(expectedResponse.getRefreshToken(), actualResponse.getRefreshToken());

        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtService, times(1)).generateToken(any(User.class));
        verify(jwtService, times(1)).generateRefreshToken(any(User.class));
        verify(userProfileService, times(1)).createProfileForUser(savedUser);
    }

    @Test
    public void AuthenticateTest() {
        // Prepare test data
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setEmail("john.doe@example.com");
        authenticationRequest.setPassword("password");

        User user = User.builder()
                .fullName("John Doe")
                .email("john.doe@example.com")
                .passwordHash("hashedPassword")
                .accountLocked(false)
                .build();

        AuthenticationResponse expectedResponse = AuthenticationResponse.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .success(true)
                .message("JWT token successfully created")
                .build();

        when(userRepository.findByEmail(authenticationRequest.getEmail())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenReturn(null); // Simulating successful authentication
        when(jwtService.generateToken(any(User.class))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");

        // Test authenticate method
        AuthenticationResponse actualResponse = authenticationService.authenticate(authenticationRequest);

        // Verify
        assertNotNull(actualResponse);
        assertEquals(expectedResponse.isSuccess(), actualResponse.isSuccess());
        assertEquals(expectedResponse.getMessage(), actualResponse.getMessage());
        assertEquals(expectedResponse.getAccessToken(), actualResponse.getAccessToken());
        assertEquals(expectedResponse.getRefreshToken(), actualResponse.getRefreshToken());

        verify(userRepository, times(1)).findByEmail(authenticationRequest.getEmail());
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtService, times(1)).generateToken(any(User.class));
        verify(jwtService, times(1)).generateRefreshToken(any(User.class));
    }
    @Test
    public void EmailAlreadyExistsTest() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFullName("John Doe");
        registerRequest.setEmail("john.doe@example.com");
        registerRequest.setPassword("password");

        when(userRepository.save(any(User.class))).thenThrow(DataIntegrityViolationException.class);

        AuthenticationResponse actualResponse = authenticationService.register(registerRequest);

        assertNotNull(actualResponse);
        assertFalse(actualResponse.isSuccess());
        assertEquals("This email is already registered. Please log in with this email or use a different one to create account", actualResponse.getMessage());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void authenticateInvalidCredentialsTest() {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setEmail("john.doe@example.com");
        authenticationRequest.setPassword("password");

        User user = User.builder()
                .fullName("John Doe")
                .email("john.doe@example.com")
                .passwordHash("hashedPassword")
                .accountLocked(false)
                .build();

        when(userRepository.findByEmail(authenticationRequest.getEmail())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenThrow(AuthenticationException.class);

        AuthenticationResponse actualResponse = authenticationService.authenticate(authenticationRequest);

        assertNotNull(actualResponse);
        assertFalse(actualResponse.isSuccess());
        assertEquals("Invalid email or password", actualResponse.getMessage());

        verify(userRepository, times(1)).findByEmail(authenticationRequest.getEmail());
        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    public void refreshTokenTest() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String authHeader = "Bearer validRefreshToken";
        String userEmail = "john.doe@example.com";

        User user = User.builder()
                .fullName("John Doe")
                .email(userEmail)
                .passwordHash("hashedPassword")
                .accountLocked(false)
                .build();

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
        when(jwtService.extractUsername("validRefreshToken")).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("validRefreshToken", user)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("newAccessToken");

        AuthenticationResponse actualResponse = authenticationService.refreshToken(request);

        assertNotNull(actualResponse);
        assertTrue(actualResponse.isSuccess());
        assertEquals("successfully created new accessToken", actualResponse.getMessage());
        assertEquals("newAccessToken", actualResponse.getAccessToken());

        verify(jwtService, times(1)).extractUsername("validRefreshToken");
        verify(userRepository, times(1)).findByEmail(userEmail);
        verify(jwtService, times(1)).isTokenValid("validRefreshToken", user);
        verify(jwtService, times(1)).generateToken(user);
        verify(tokenRepository, times(1)).saveAll(anyList());
    }


    @Test
    public void revokeAllUserTokensTest() {
        User user = User.builder()
                .fullName("John Doe")
                .email("john.doe@example.com")
                .passwordHash("hashedPassword")
                .accountLocked(false)
                .build();

        Token token = Token.builder()
                .user(user)
                .token("validToken")
                .expired(false)
                .revoked(false)
                .build();

        when(tokenRepository.findAllValidTokenByUser(user.getId())).thenReturn(Collections.singletonList(token));

        authenticationService.revokeAllUserTokens(user);

        assertTrue(token.isExpired());
        assertTrue(token.isRevoked());

        verify(tokenRepository, times(1)).findAllValidTokenByUser(user.getId());
        verify(tokenRepository, times(1)).saveAll(anyList());
    }


}
