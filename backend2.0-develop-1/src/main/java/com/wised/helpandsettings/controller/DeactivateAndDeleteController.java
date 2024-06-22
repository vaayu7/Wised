package com.wised.helpandsettings.controller;

import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.UserProfileRepository;
import com.wised.helpandsettings.dtos.DeactivateOrDeleteRequest;
import com.wised.helpandsettings.service.DeactivateAndDeleteService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/deactivateanddelete")
@AllArgsConstructor
public class DeactivateAndDeleteController {

    private final DeactivateAndDeleteService deactivateAndDeleteService;
    private final UserProfileRepository userProfileRepository;

    @PostMapping("/deactivate")
    public void deactivateAccount(@RequestBody DeactivateOrDeleteRequest request) {
        try {

            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail = currentUser.getUsername();
            String password = currentUser.getPassword();

            // Retrieve the user's profile from the repository
            Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

            if(!userProfileOptional.isPresent()) {
                 return ;
            }

            UserProfile userProfile = userProfileOptional.get();
            deactivateAndDeleteService.deactivateAccount(userProfile, password, request, true);
        }
        catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @PostMapping("/initiateDelete")
    public void initiateDeleteAccount(@RequestBody DeactivateOrDeleteRequest request) {
        try{
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail = currentUser.getUsername();
            String password = currentUser.getPassword();

            // Retrieve the user's profile from the repository
            Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

            if (!userProfileOptional.isPresent()) {
                  return ;
            }

            UserProfile userProfile = userProfileOptional.get();

            deactivateAndDeleteService.initiateDeleteAccount(userProfile, password, request);
//            return ResponseEntity.ok(response);
        }

        catch (Exception e) {
           // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
