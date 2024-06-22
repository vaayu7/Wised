package com.wised.auth.controller;

import com.wised.auth.dtos.UserProfileRequest;
import com.wised.auth.dtos.UserProfileResponse;
import com.wised.auth.dtos.UsernameAvailabilityResponse;
import com.wised.auth.mappers.UserProfileMapper;
import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.auth.model.UserProfileData;
import com.wised.auth.repository.UserProfileRepository;
import com.wised.auth.service.UserProfileService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller for managing user profiles.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/user/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;

    /**
     * Get the user's profile data.
     *
     * @return A ResponseEntity with UserProfileResponse containing the user's profile data.
     */
    @GetMapping("")
    public ResponseEntity<UserProfileResponse> getUserProfileData() {
        try {
            // Get the currently authenticated user
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail = currentUser.getUsername();

            // Retrieve the user's profile from the repository
            Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

            if (!userProfileOptional.isPresent()) {
                return ResponseEntity.badRequest().body(UserProfileResponse.builder()
                        .success(false)
                        .error("Invalid user")
                        .message("User profile not found")
                        .build());
            }

            UserProfile userProfile = userProfileOptional.get();
            userProfile.setEmail(userEmail);

            // Convert UserProfile to UserProfileData using the mapper
            UserProfileData userProfileData = userProfileService.getProfile(userProfile);

            return ResponseEntity.ok().body(UserProfileResponse.builder()
                    .success(true)
                    .message("Successfully fetched user profile")
                    .data(userProfileData)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.ok().body(UserProfileResponse.builder()
                    .success(false)
                    .error(e.getMessage())
                    .message("Error while fetching user data")
                    .build());
        }
    }


    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> getOtherUserProfileData(@PathVariable String username) {
        try {
            // Get the currently authenticated user
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail = currentUser.getUsername();
            // Retrieve the user's profile from the repository
            Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

            if (!userProfileOptional.isPresent()) {
                return ResponseEntity.badRequest().body(UserProfileResponse.builder()
                        .success(false)
                        .error("Invalid user")
                        .message("User profile not found")
                        .build());
            }

            UserProfile userProfile = userProfileOptional.get();
            userProfile.setEmail(userEmail);

            // Convert UserProfile to UserProfileData using the mapper
            UserProfileData userProfileData = userProfileService.getProfile(userProfile, username);

            return ResponseEntity.ok().body(UserProfileResponse.builder()
                    .success(true)
                    .message("Successfully fetched user profile")
                    .data(userProfileData)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.ok().body(UserProfileResponse.builder()
                    .success(false)
                    .error(e.getMessage())
                    .message("Error while fetching user data")
                    .build());
        }
    }



    /**
     * Update the user's profile.
     *
     * @param profileRequest The UserProfileRequest containing the updated profile data.
     * @return A ResponseEntity with UserProfileResponse indicating the result of the update.
     */
    @PutMapping("/update")
    public ResponseEntity<UserProfileResponse> updateProfile(@RequestBody UserProfileRequest profileRequest) {
        try {
            // Get the authenticated user's details
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail = currentUser.getUsername();

            // Retrieve the user's profile from the repository
            Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

            if (!userProfileOptional.isPresent()) {
                return ResponseEntity.badRequest().body(UserProfileResponse.builder()
                        .success(false)
                        .error("Invalid user")
                        .message("User profile not found")
                        .build());
            }

            UserProfile userProfile = userProfileOptional.get();
            userProfile.setEmail(userEmail);

            // Call the service to update the user's profile
            UserProfileResponse userProfileResponse = userProfileService.updateProfileFields(userProfile, profileRequest);

            if (userProfileResponse.isSuccess()) {
                return ResponseEntity.ok(userProfileResponse); // Successful update, HTTP 200 OK
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(userProfileResponse);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(UserProfileResponse.builder()
                    .success(false)
                    .error(e.getMessage())
                    .message("Error occurred while updating user profile")
                    .build());
        }
    }

    @GetMapping("/username-check")
    public ResponseEntity<UsernameAvailabilityResponse> checkUserNameAvailability(@RequestParam String username) {
        try {
            // Check if the requested username is available
            boolean isUserNameAvailable = userProfileService.isUserNameAvailable(username);
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail = currentUser.getUsername();

            // Retrieve the user's profile from the repository
            Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

            if (!userProfileOptional.isPresent()) {
                return ResponseEntity.badRequest().body(UsernameAvailabilityResponse.builder()
                        .error("Invalid user")// No suggestions needed if username is available
                        .build());
            }

            UserProfile userProfile = userProfileOptional.get();
            if (isUserNameAvailable) {
                return ResponseEntity.ok(UsernameAvailabilityResponse.builder()
                        .available(true)
                        .suggestions(null) // No suggestions needed if username is available
                        .build());
            } else {
                // Suggest unique usernames if the requested username is taken
                List<String> suggestions = userProfileService.suggestUniqueUsernames(username);

                return ResponseEntity.ok(UsernameAvailabilityResponse.builder()
                        .available(false)
                        .suggestions(suggestions)
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(UsernameAvailabilityResponse.builder()
                    .available(false)
                    .suggestions(null)
                    .build());
        }
    }
}

