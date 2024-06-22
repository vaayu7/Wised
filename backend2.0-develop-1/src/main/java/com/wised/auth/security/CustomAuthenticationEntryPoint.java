package com.wised.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wised.auth.dtos.CustomAuthenticationEntryPointResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * The `CustomAuthenticationEntryPoint` class implements the AuthenticationEntryPoint interface
 * to customize the error response for unauthorized requests.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Commences the authentication process for unauthorized requests.
     *
     * @param request       The HTTP request that resulted in an authentication failure.
     * @param response      The HTTP response to be customized for the error message.
     * @param authException An AuthenticationException that caused the authentication failure.
     * @throws IOException      If an I/O error occurs while writing the response.
     * @throws ServletException If a servlet error occurs.
     */

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // Customize the error response
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
        CustomAuthenticationEntryPointResponse res = CustomAuthenticationEntryPointResponse.builder()
                .error(authException.getMessage())
                .message("Access denied due to missing or invalid token.")
                .success(false)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(response.getWriter(), res);
    }
}