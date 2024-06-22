package com.wised.auth.service;


import io.jsonwebtoken.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Service class for handling JSON Web Tokens (JWTs).
 */
@Service
public class JwtService {

    // Secret key used for signing and verifying JWTs

    @Value("${application.security.jwt.secret-key}")
    private  String secretKey;

    @Value("${application.security.jwt.expiration}")
    private  long jwtExpiration;

    @Value("${application.security.jwt.reset-token.expiration}")
    private long resetTokenExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private  long refreshExpiration;


    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setJwtExpiration(long jwtExpiration) {
        this.jwtExpiration = jwtExpiration;
    }

    public void setResetTokenExpiration(long resetTokenExpiration) {
        this.resetTokenExpiration = resetTokenExpiration;
    }

    public void setRefreshExpiration(long refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }

    /**
     * Extract the username from a JWT token.
     * @param token The JWT token from which to extract the username.
     * @return The extracted username.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Generate a JWT token for a user.
     * @param userDetails The UserDetails object containing user information.
     * @return The generated JWT token.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generate a JWT token with extra claims for a user.
     * @param extraClaims Additional claims to include in the token.
     * @param userDetails The UserDetails object containing user information.
     * @return The generated JWT token with extra claims.
     */
    public String generateToken(Map<String, Objects> extraClaims,
                                UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    public String generateResetToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, resetTokenExpiration);
    }

    private  String buildToken(
            Map<String, Objects> extraClaims,
            UserDetails userDetails,
            long expiration
    ){
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Check if a JWT token is valid for a given user.
     * @param token The JWT token to validate.
     * @param userDetails The UserDetails object containing user information.
     * @return True if the token is valid, false otherwise.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // Private methods for internal token processing

    /**
     * Check if a JWT token has expired.
     * @param token The JWT token to check.
     * @return True if the token has expired, false if it's still valid.
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration != null && expiration.getTime() <= System.currentTimeMillis();
        } catch (ExpiredJwtException e) {
            // Token parsing or other error
            System.out.println("token expired " + e);
            return true; // Consider it expired if an error occurs
        } catch (Exception e) {
            System.out.println(" Some other exception in JWT parsing ");
            return false;
        }
    }

    /**
     * Extract the expiration date from a JWT token.
     * @param token The JWT token from which to extract the expiration date.
     * @return The expiration date as a Date object.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract a specific claim from a JWT token.
     * @param token The JWT token from which to extract the claim.
     * @param claimsResolver A function to extract a specific claim from the token's claims.
     * @param <T> The type of the extracted claim.
     * @return The extracted claim.
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from a JWT token.
     * @param token The JWT token from which to extract the claims.
     * @return A Claims object containing all the claims from the token.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Get the signing key used for JWT token validation.
     * @return The signing key as a Key object.
     */
    public Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}