package com.wised.auth.config;


import com.wised.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class for defining application-specific configurations, such as user details, authentication provider,
 * and password encoding.
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository repository; // Repository for user data


    /**
     * Define a custom UserDetailsService bean for loading user details by email.
     *
     * @return A UserDetailsService implementation.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Define an AuthenticationProvider bean for user authentication.
     * This provider uses a UserDetailsService and a password encoder.
     *
     * @return An AuthenticationProvider implementation.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // Create a DaoAuthenticationProvider with a UserDetailsService and password encoder
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Define an AuthenticationManager bean for handling user authentication.
     *
     * @param config AuthenticationConfiguration for obtaining the AuthenticationManager.
     * @return An AuthenticationManager instance.
     * @throws Exception If an exception occurs while configuring the AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Define a PasswordEncoder bean for encoding and verifying passwords.
     *
     * @return A PasswordEncoder implementation (BCryptPasswordEncoder in this case).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public OtlpGrpcSpanExporter otlpHttpSpanExporter(@Value("${tracing.url}") String url) {
//        return OtlpGrpcSpanExporter.builder().setEndpoint(url).build();
//    }

//    @Bean
//    public static JaegerTracer getTracer() {
//        io.jaegertracing.Configuration.SamplerConfiguration samplerConfig =
//                io.jaegertracing.Configuration.SamplerConfiguration.fromEnv().withType("const").withParam(1);
//        io.jaegertracing.Configuration.ReporterConfiguration reporterConfig =
//                io.jaegertracing.Configuration.ReporterConfiguration.fromEnv().withLogSpans(true);
//        io.jaegertracing.Configuration config = new io.jaegertracing.Configuration("fooService").withSampler(samplerConfig).withReporter(reporterConfig);
//        return config.getTracer();
//    }
//
//    @PostConstruct
//    public void setProperty() {
//        System.setProperty("JAEGER_REPORTER_LOG_SPANS", "true");
//    }


}





