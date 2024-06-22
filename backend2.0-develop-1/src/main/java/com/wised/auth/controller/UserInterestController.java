package com.wised.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wised.auth.dtos.UserInterestResponse;
import com.wised.auth.dtos.UserInterestRequest;
import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.UserProfileRepository;
import com.wised.auth.service.UserInterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/user/profile/interests")
@RequiredArgsConstructor
public class UserInterestController {

    private final UserInterestService userInterestService;
    private final UserProfileRepository userProfileRepository;

    @GetMapping("")
    public ResponseEntity<String> getInterestJsonFile() {
        try {
            ClassPathResource resource = new ClassPathResource("Interest.json");
            String jsonContent = new String(resource.getInputStream().readAllBytes());
            return ResponseEntity.ok(jsonContent);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/getinterests")
    public ResponseEntity<UserInterestResponse> getUserInterests() {
        try {
            // Fetch current user profile
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail = currentUser.getUsername();

            // Retrieve the user's profile from the repository
            Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

            if (!userProfileOptional.isPresent()) {
                return ResponseEntity.badRequest().body(UserInterestResponse.builder()
                        .error("Invalid user")
                        .build());
            }

            UserProfile userProfile = userProfileOptional.get();

            List<String> userInterests = userInterestService.getUserInterests(userProfile);

            return ResponseEntity.ok().body(UserInterestResponse.builder()
                    .message("Interests fetched successfully")
                    .data(userInterests)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(UserInterestResponse.builder()
                    .error("Failed to add interests")
                    .build());
        }
    }

    @PostMapping("/addinterest")
    public ResponseEntity<UserInterestResponse> addInterests(@RequestBody UserInterestRequest request) {
        try {
            // Fetch current user profile
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail = currentUser.getUsername();

            // Retrieve the user's profile from the repository
            Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

            if (!userProfileOptional.isPresent()) {
                return ResponseEntity.badRequest().body(UserInterestResponse.builder()
                        .error("Invalid user")
                        .build());
            }

            UserProfile userProfile = userProfileOptional.get();

            // Call service to add user interests
            userInterestService.addUserInterests(userProfile, request.getInterests(), request.getOtherInterestsList());

            return ResponseEntity.ok().body(UserInterestResponse.builder()
                    .message("Interests added successfully")
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(UserInterestResponse.builder()
                    .error("Failed to add interests")
                    .build());
        }
    }

    @DeleteMapping("/removeinterest/{interestId}")
    public ResponseEntity<UserInterestResponse> removeInterest(@PathVariable Integer interestId) {
        try {
            // Fetch current user profile
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail = currentUser.getUsername();

            // Retrieve the user's profile from the repository
            Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

            if (!userProfileOptional.isPresent()) {
                return ResponseEntity.badRequest().body(UserInterestResponse.builder()
                        .error("Invalid user")
                        .build());
            }

            UserProfile userProfile = userProfileOptional.get();

            // Call service to remove user interest
            userInterestService.removeUserInterest(userProfile, interestId);

            return ResponseEntity.ok().body(UserInterestResponse.builder()
                    .message("Interest removed successfully")
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(UserInterestResponse.builder()
                    .error("Failed to remove interest")
                    .build());
        }
    }

    @GetMapping("/search/{keyword}")
    public ResponseEntity<UserInterestResponse> searchInterests(@PathVariable String keyword) {
        try {
            // Load interests from interests.json file
            List<String> allInterests = loadInterestsFromJson();

            // Filter interests based on the keyword
            List<String> filteredInterests = allInterests.stream()
                    .filter(interest -> interest.toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());

            if (filteredInterests.isEmpty()) {
                return ResponseEntity.ok().body(UserInterestResponse.builder()
                        .message("No matches found for '" + keyword + "'")
                        .data(Collections.emptyList())
                        .build());
            } else {
                return ResponseEntity.ok().body(UserInterestResponse.builder()
                        .message("Interests filtered successfully")
                        .data(filteredInterests)
                        .build());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(UserInterestResponse.builder()
                    .error("Failed to load interests from file")
                    .build());
        }
    }

    private List<String> loadInterestsFromJson() throws IOException {
        ClassPathResource resource = new ClassPathResource("interest.json");
        String jsonContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonContent);

        List<String> interests = new ArrayList<>();
        if (rootNode.has("interests")) {
            JsonNode interestsNode = rootNode.get("interests");
            if (interestsNode.isArray()) {
                for (JsonNode interest : interestsNode) {
                    interests.add(interest.asText());
                }
            }
        }

        return interests;
    }
}
