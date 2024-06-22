package com.wised.auth.controller;

import com.wised.auth.dtos.ProfessionalDetailRequest;
import com.wised.auth.dtos.ProfessionalDetailResponse;
import com.wised.auth.dtos.ProfessionalDetailsDTO;
import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.UserProfileRepository;
import com.wised.auth.service.ProfessionalDetailService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user/profile/professional-details")
@AllArgsConstructor
public class ProfessionalDetailsController {

    private final ProfessionalDetailService professionalDetailService;
    private final UserProfileRepository userProfileRepository;

    @PostMapping
    public ResponseEntity<ProfessionalDetailResponse> createProfessionalDetail(@RequestBody ProfessionalDetailRequest request) {
        try {
            UserProfile userProfile = getUserProfileFromContext();
            if (userProfile == null) {
                return ResponseEntity.badRequest().body(ProfessionalDetailResponse.builder().error("Invalid user").build());
            }

            ProfessionalDetailResponse response = professionalDetailService.createProfessionalDetail(userProfile, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfessionalDetailResponse> getProfessionalDetail(@PathVariable("id") int id) {
        try {
            UserProfile userProfile = getUserProfileFromContext();
            if (userProfile == null) {
                return ResponseEntity.badRequest().body(ProfessionalDetailResponse.builder().error("Invalid user").build());
            }

            ProfessionalDetailResponse details = professionalDetailService.getProfessionalDetailById(userProfile, id);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfessionalDetailResponse> updateProfessionalDetail(
            @PathVariable("id") int id, @RequestBody ProfessionalDetailRequest request) {
        try {
            UserProfile userProfile = getUserProfileFromContext();
            if (userProfile == null) {
                return ResponseEntity.badRequest().body(ProfessionalDetailResponse.builder().error("Invalid user").build());
            }

            ProfessionalDetailResponse response = professionalDetailService.updateProfessionalDetail(userProfile, id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfessionalDetail(@PathVariable("id") int id) {
        try {
            UserProfile userProfile = getUserProfileFromContext();
            if (userProfile == null) {
                return ResponseEntity.badRequest().build();
            }

            professionalDetailService.deleteProfessionalDetail(userProfile, id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private UserProfile getUserProfileFromContext() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userEmail = currentUser.getUsername();
        Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);
        return userProfileOptional.orElse(null);
    }
}
