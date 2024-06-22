package com.wised.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wised.auth.model.Token;
import com.wised.auth.repository.TokenRepository;
import com.wised.auth.dtos.CustomLogoutResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LogoutServiceTest {

    @InjectMocks
    private LogoutService logoutService;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private PrintWriter printWriter;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void testLogoutSuccess() throws Exception {
        String jwt = "testJwt";
        String authHeader = "Bearer " + jwt;
        String userEmail = "test@example.com";

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(jwt)).thenReturn(userEmail);
        when(tokenRepository.findByToken(jwt)).thenReturn(java.util.Optional.of(mock(Token.class)));

        logoutService.logout(request, response, authentication);

        ArgumentCaptor<CustomLogoutResponse> responseCaptor = ArgumentCaptor.forClass(CustomLogoutResponse.class);
        verify(printWriter).write(anyString());
        verify(printWriter).flush();

        CustomLogoutResponse logoutResponse = responseCaptor.getValue();
        assertNotNull(logoutResponse);
        assertTrue(logoutResponse.isSuccess());
        assertEquals("Successfully logged out", logoutResponse.getMessage());
        assertEquals(userEmail, logoutResponse.getEmail());

        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @Test
    void testLogoutUserNotFound() throws Exception {
        String jwt = "testJwt";
        String authHeader = "Bearer " + jwt;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(jwt)).thenThrow(new UsernameNotFoundException("User not found"));

        logoutService.logout(request, response, authentication);

        ArgumentCaptor<CustomLogoutResponse> responseCaptor = ArgumentCaptor.forClass(CustomLogoutResponse.class);
        verify(printWriter).write(anyString());
        verify(printWriter).flush();

        CustomLogoutResponse logoutResponse = responseCaptor.getValue();
        assertNotNull(logoutResponse);
        assertFalse(logoutResponse.isSuccess());
        assertEquals("User not found", logoutResponse.getMessage());
        assertEquals("User not found", logoutResponse.getError());

        verify(tokenRepository, never()).save(any(Token.class));
    }

    @Test
    void testLogoutOtherErrors() throws Exception {
        String jwt = "testJwt";
        String authHeader = "Bearer " + jwt;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(jwt)).thenThrow(new RuntimeException("Unexpected error"));

        logoutService.logout(request, response, authentication);

        ArgumentCaptor<CustomLogoutResponse> responseCaptor = ArgumentCaptor.forClass(CustomLogoutResponse.class);
        verify(printWriter).write(anyString());
        verify(printWriter).flush();

        CustomLogoutResponse logoutResponse = responseCaptor.getValue();
        assertNotNull(logoutResponse);
        assertFalse(logoutResponse.isSuccess());
        assertEquals("An error occurred while logging out", logoutResponse.getMessage());
        assertEquals("Unexpected error", logoutResponse.getError());

        verify(tokenRepository, never()).save(any(Token.class));
    }
}

