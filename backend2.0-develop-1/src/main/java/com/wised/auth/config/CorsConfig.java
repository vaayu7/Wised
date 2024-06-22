package com.wised.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Configuration class for CORS (Cross-Origin Resource Sharing) setup.
 */
@Configuration
public class CorsConfig {

    /**
     * Creates a CorsFilter bean to configure CORS settings.
     *
     * @return A CorsFilter bean with specified CORS configuration.
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Add the allowed origin (in this case, http://localhost:5173)
        corsConfig.addAllowedOrigin("http://localhost:5173");

        // Allow all headers and methods (you may adjust these based on your requirements)
        corsConfig.addAllowedHeader("*");
        corsConfig.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // Apply the CORS configuration to all paths ("/**")
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsFilter(source);
    }

}

