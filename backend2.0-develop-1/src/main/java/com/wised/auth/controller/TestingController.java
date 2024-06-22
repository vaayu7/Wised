package com.wised.auth.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * The `TestingController` class provides endpoints for testing the functionality of the application.
 * It includes endpoints for checking if the application is running and testing secured routes.
 */
@RestController
@RequiredArgsConstructor
public class TestingController {


//    @Autowired
//    private OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> oAuth2AccessTokenResponseClient;

    /**
     * A simple endpoint to check if the application is running.
     *
     * @return A "Hello, it is working" message indicating that the application is operational.
     */
    @GetMapping("/home")
    public String home() {
        return "Hello, it is working";
    }


//    @GetMapping("/api/user/google")
//    public ResponseEntity<?> getUserFromGoogle(@AuthenticationPrincipal OAuth2User oauth2User) {
//        if (oauth2User == null) {
//            return ResponseEntity.badRequest().body("No user");
//        } else {
//            return ResponseEntity.ok().body(oauth2User.getAttributes());
//        }
//    }
//
//    @GetMapping("/api/user/facebook")
//    public ResponseEntity<?> getUserFromFacebook(@AuthenticationPrincipal OAuth2User oauth2User) {
//        if (oauth2User == null) {
//            return ResponseEntity.badRequest().body("No user");
//        } else {
//            return ResponseEntity.ok().body(oauth2User.getAttributes());
//        }
//    }


    /**
     * An endpoint to test access to a secured route.
     *
     * @return A message indicating that the secured route is also working.
     */
    @GetMapping("/secured")
    public String secured() {
        try {
            return "Secured route is also working";
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
