package com.wised.helpandsettings.controller;

import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.UserProfileRepository;
import com.wised.helpandsettings.dtos.HelpRequest;
import com.wised.helpandsettings.dtos.HelpResponse;
import com.wised.helpandsettings.enums.HelpStatus;
import com.wised.helpandsettings.service.HelpService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/help")
@AllArgsConstructor
public class HelpController {

    private final HelpService helpService;

    private final UserProfileRepository userProfileRepository;

    @PostMapping("/create-help")
    public ResponseEntity<HelpResponse> createHelpRequest(@RequestBody HelpRequest helpRequest) {
        try {
            // Fetch user profile from repository based on userId
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail = currentUser.getUsername();

            // Retrieve the user's profile from the repository
            Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

            if (!userProfileOptional.isPresent()) {
                return ResponseEntity.badRequest().body(HelpResponse.builder()
                        .error("Invalid user")
                        .build());
            }

            UserProfile userProfile = userProfileOptional.get();
            System.out.println("calling help request");
            HelpResponse response = helpService.createHelpRequest(userProfile, helpRequest);
            System.out.println("calling help request done");
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/update-help-status")
    public ResponseEntity<Boolean> updateHelpStatus(@RequestParam("helpId") Integer helpId, @RequestParam("helpStatus") HelpStatus helpStatus){
        try {
            boolean statusUpdated = helpService.updateHelpStatus(helpId, helpStatus);
            return ResponseEntity.ok().body(statusUpdated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/get-user-help-requests")
    public ResponseEntity<HelpResponse> getHelpRequestsByUser() {
        try {
            // Fetch user profile from repository based on userId
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail = currentUser.getUsername();

            // Retrieve the user's profile from the repository
            Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

            if (!userProfileOptional.isPresent()) {
                return ResponseEntity.badRequest().body(HelpResponse.builder()
                        .error("Invalid user")
                        .build());
            }

            UserProfile userProfile = userProfileOptional.get();
            HelpResponse response = helpService.getHelpRequestsByUser(userProfile);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

