package com.wised.post.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wised.auth.dtos.UserInterestResponse;
import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.UserProfileRepository;
import com.wised.helpandsettings.dtos.HelpResponse;
import com.wised.people.dtos.PeopleResponse;
import com.wised.people.dtos.ReportRequest;
import com.wised.post.dtos.LikeOrDislikeRequest;
import com.wised.post.dtos.PostRequest;
import com.wised.post.dtos.PostResponse;
import com.wised.post.dtos.ReportPostRequest;
import com.wised.post.service.PostService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/posts")
@AllArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserProfileRepository userProfileRepository;


    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello!");
    }

    @PostMapping("/createPost")
    public ResponseEntity<PostResponse> createPost(@RequestBody PostRequest postRequest) {
        PostResponse response;
        try {
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail = currentUser.getUsername();

            // Retrieve the user's profile from the repository
            Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

            if (!userProfileOptional.isPresent()) {
                return ResponseEntity.badRequest().body(PostResponse.builder()
                        .error("Invalid user")
                        .build());
            }
            System.out.println("entered");
            UserProfile userProfile = userProfileOptional.get();
             response = postService.createpost(postRequest, userProfile);
            System.out.println("entered 8");
        } catch (Exception e) {
            // Log the exception and return an internal server error response
//            e.printStackTrace(); // Replace with a proper logging mechanism
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/like")
    public ResponseEntity<String> likeOrDislikePost(@RequestBody LikeOrDislikeRequest likeRequest) {
        try {
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail = currentUser.getUsername();

            // Retrieve the user's profile from the repository
            Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

            if (!userProfileOptional.isPresent()) {
                return ResponseEntity.badRequest().body("Invalid user!");
            }

            UserProfile userProfile = userProfileOptional.get();
            postService.likeDislikePost(likeRequest.getPostId(), userProfile, likeRequest.getLikeType());

            return ResponseEntity.ok("Successfully updated like status");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the request");
        }
    }

    @GetMapping("/user-posts/{username}")
    public ResponseEntity<PostResponse> getUserPosts(@PathVariable String username) {
        try {
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail = currentUser.getUsername();

            // Retrieve the user's profile from the repository
            Optional<UserProfile> userViewerProfileOptional = userProfileRepository.findByUserEmail(userEmail);

            if (!userViewerProfileOptional.isPresent()) {
                return ResponseEntity.badRequest().body(PostResponse.builder()
                        .error("Invalid user")
                        .build());
            }
            UserProfile userViewerProfile = userViewerProfileOptional.get();

            Optional<UserProfile> userOwnerProfileOptional = userProfileRepository.findByUserName(username);

            if (!userOwnerProfileOptional.isPresent()) {
                return ResponseEntity.badRequest().body(PostResponse.builder()
                        .error("Invalid user")
                        .build());
            }


            UserProfile userOwnerProfile = userOwnerProfileOptional.get();
            return ResponseEntity.ok().body(postService.getUserPosts(userViewerProfile, userOwnerProfile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(PostResponse.builder()
                    .error("Failed to retrieve user posts")
                    .message(e.getMessage())
                    .build());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<PostResponse> getAllPosts() {
        try {
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail = currentUser.getUsername();

            // Retrieve the user's profile from the repository
            Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

            if (!userProfileOptional.isPresent()) {
                return ResponseEntity.badRequest().body(PostResponse.builder()
                        .error("Invalid user")
                        .build());
            }

            UserProfile userProfile = userProfileOptional.get();
            return ResponseEntity.ok().body(postService.getAllPosts());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(PostResponse.builder()
                    .error("Failed to retrieve all posts")
                    .message(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/report")
    public ResponseEntity<PostResponse> reportPost(@RequestBody ReportPostRequest reportRequest) {
        try {
            // Get the currently authenticated user
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail = currentUser.getUsername();

            // Retrieve the user's profile from the repository
            Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

            if (!userProfileOptional.isPresent()) {
                return ResponseEntity.badRequest().body(PostResponse.builder()
                        .error("Invalid user")
                        .build());
            }

            UserProfile reportingUserProfile = userProfileOptional.get();

            PostResponse response = postService.reportPosts(reportingUserProfile, reportRequest);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.status(org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/incr-share-count/{postId}")
    public ResponseEntity<String> sharePost(@PathVariable Integer postId) {
        try {
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail = currentUser.getUsername();

            // Retrieve the user's profile from the repository
            Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

            if (!userProfileOptional.isPresent()) {
                return ResponseEntity.badRequest().body("Invalid user!");
            }

            UserProfile userProfile = userProfileOptional.get();
            postService.SharePost(postId, userProfile);
            return ResponseEntity.ok("Successfully updated share count");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the request");
        }
    }
    @GetMapping("/category/{keyword}")
    public ResponseEntity<UserInterestResponse> searchInterests(@PathVariable String keyword) {
        try {
            List<String> allInterests = loadInterestsFromJson();
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
                        .message("categories filtered successfully")
                        .data(filteredInterests)
                        .build());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(UserInterestResponse.builder()
                    .error("Failed to load categories from file")
                    .build());
        }
    }

    private List<String> loadInterestsFromJson() throws IOException {
        ClassPathResource resource = new ClassPathResource("category.json");
        String jsonContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonContent);

        List<String> interests = new ArrayList<>();
        if (rootNode.has("category")) {
            JsonNode interestsNode = rootNode.get("category");
            if (interestsNode.isArray()) {
                for (JsonNode interest : interestsNode) {
                    interests.add(interest.asText());
                }
            }
        }

        return interests;
    }

}