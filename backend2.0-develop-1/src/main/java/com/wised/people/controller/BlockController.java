package com.wised.people.controller;

import com.wised.auth.exception.UserNotFoundException;
import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.UserProfileRepository;
import com.wised.auth.service.UserInterestService;
import com.wised.people.dtos.UserBlockResponse;
import com.wised.people.exception.UserBlockedException;
import com.wised.people.service.BlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/people/block")
@RequiredArgsConstructor
public class BlockController {

    private final UserProfileRepository userProfileRepository;
    private final BlockService blockService;

    // Existing methods for fetching, adding, and removing interests

    @PostMapping("/block-user/{username}")
    public ResponseEntity<UserBlockResponse> blockUser(@PathVariable String username) {
        try {
            // Fetch the current user profile
            UserProfile blockerUserProfile = getCurrentUserProfile();
            System.out.println("started 1");
            // Fetch the user profile of the user to be blocked

            // Block the user
            blockService.blockUser(username);
            System.out.println("started ");
            return ResponseEntity.ok().body(UserBlockResponse.builder()
                    .message("User blocked successfully")
                    .build());
        } catch (UserBlockedException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(UserBlockResponse.builder()
                    .error("User has been already blocked by this user")
                    .build());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(UserBlockResponse.builder()
                    .error("Failed to block user")
                    .build());
        }
    }

    @PostMapping("/unblock-user/{username}")
    public ResponseEntity<UserBlockResponse> unblockUser(@PathVariable String username) {
        try {
            // Fetch the current user profile
            UserProfile blockerUserProfile = getCurrentUserProfile();

            // Fetch the user profile of the user to be unblocked

            // Unblock the user
            blockService.unblockUser(username);

            return ResponseEntity.ok().body(UserBlockResponse.builder()
                    .message("User unblocked successfully")
                    .build());
        } catch (UserBlockedException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(UserBlockResponse.builder()
                    .error("User has not been blocked by  this user")
                    .build());
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(UserBlockResponse.builder()
                    .error("Failed to unblock user")
                    .build());
        }
    }

    // Helper method to fetch the current user's profile
    private UserProfile getCurrentUserProfile() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userProfileRepository.findByUserEmail(currentUser.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Current user profile not found"));
    }
}

