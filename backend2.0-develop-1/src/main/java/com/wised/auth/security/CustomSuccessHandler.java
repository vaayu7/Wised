package com.wised.auth.security;

import com.wised.auth.dtos.AuthenticationResponse;
import com.wised.auth.dtos.RegisterRequest;
import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.UserRepository;
import com.wised.auth.service.JwtService;
import com.wised.auth.service.UserProfileService;
import com.wised.auth.helper.PasswordGenerator;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;


/**
 * Custom authentication success handler for handling successful login/authentication.
 */
@Component
@AllArgsConstructor
public class CustomSuccessHandler implements AuthenticationSuccessHandler {



    private final UserRepository userRepo; // Repository for user data
    private final JwtService jwtService; // JWT service for token generation
    private  final UserProfileService userProfileService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Handle a successful authentication event.
     *
     * @param request        The HTTP request object.
     * @param response       The HTTP response object.
     * @param authentication The authentication object representing the authenticated user.
     * @throws IOException      If an I/O error occurs while writing to the response.
     * @throws ServletException If a servlet error occurs.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {



        String redirectionUrl = "http://localhost:5173/google_redirect" + "?refresh_token=";
        try {
            System.out.println("custom onAuthenticationSuccess ...................");
            if (authentication.getPrincipal() instanceof DefaultOAuth2User) {

                DefaultOAuth2User userDetails = (DefaultOAuth2User) authentication.getPrincipal();

                String email = userDetails.getAttribute("email") != null ? userDetails.getAttribute("email") : null;

                String name = userDetails.getAttribute("name");

                if (name == null || name.isEmpty()) {
                    name = email.split("@")[0];
                }
                Optional<User> optionalUser = userRepo.findByEmail(email);
                AuthenticationResponse authResponse;

                System.out.println(email);
                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();

                    // Generate a JWT token for the authenticated user
                    var refreshToken = jwtService.generateRefreshToken(user);
                    // Prepare an AuthenticationResponse containing the JWT token

                    redirectionUrl +=  refreshToken;
                    System.out.println(redirectionUrl);

                    new DefaultRedirectStrategy().sendRedirect(request, response, redirectionUrl);


                } else {
                    // User not found in the repository

                    String randomPassword = PasswordGenerator.generateRandomPassword(12); // Adjust the password length as needed
                    RegisterRequest custom_request1 = RegisterRequest.builder()
                            .email(email)
                            .password(randomPassword)
                            .fullName(name)
                            .build();

                    var user = User.builder()
                            .fullName(name)
                            .email(email)
                            .accountLocked(false)
                            .failedAttempt(0)
                            .passwordHash(passwordEncoder.encode(randomPassword))
                            .role(null)
                            .build();

                    // Save the user to the repository
                    var savedUser = userRepo.save(user);

                    //creating profile entry for user
                    UserProfile userProfile = userProfileService.createProfileForUser(savedUser);


                    var refreshToken = jwtService.generateRefreshToken(user);



                    redirectionUrl +=  refreshToken;
                    System.out.println(redirectionUrl);

                    new DefaultRedirectStrategy().sendRedirect(request, response, redirectionUrl);

                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());

            new DefaultRedirectStrategy().sendRedirect(request, response, redirectionUrl);
        }
    }

}