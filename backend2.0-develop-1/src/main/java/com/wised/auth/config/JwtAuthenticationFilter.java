package com.wised.auth.config;

import com.wised.auth.repository.TokenRepository;
import com.wised.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


/**
 * JWT authentication filter for verifying and processing JWT tokens in HTTP requests.
 */
@Component
@Configuration
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService; // JWT service for token handling
    private final UserDetailsService userDetailsService; // UserDetailsService for loading user details
    private  final TokenRepository tokenRepository;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /**
     * Check the JWT token for every HTTP request and retrieve the token from the request header.
     * If the token is not present or does not start with "Bearer ", it will be ignored.
     *
     * @param request     The HTTP request object.
     * @param response    The HTTP response object.
     * @param filterChain The filter chain for processing the request.
     * @throws ServletException If a servlet error occurs.
     * @throws IOException      If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                // If the Authorization header is missing or doesn't start with "Bearer ",
                // continue processing the request without authentication.
                filterChain.doFilter(request, response);
                return;
            }

            jwt = authHeader.substring(7); // Remove "Bearer " prefix
            userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // If the user email is present in the token and there is no existing authentication context,
                // attempt to load user details and validate the token.
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                boolean isTokenValid = tokenRepository.findByToken(jwt)
                        .map(t -> !t.isExpired() && !t.isRevoked())
                        .orElse(false);

                if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {
                    // If the token is valid, create an authentication token and set it in the security context.
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (UsernameNotFoundException e) {
            // Handle UsernameNotFoundException: User not found
            logger.error("User not found while processing JWT authentication.", e);
        } catch (Exception e) {
            // Handle other exceptions
            logger.error("Error processing JWT authentication.", e);
        }

        filterChain.doFilter(request, response);
    }
}

