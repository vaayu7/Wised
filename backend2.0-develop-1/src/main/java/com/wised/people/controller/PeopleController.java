package com.wised.people.controller;

import com.wised.auth.exception.UserNotFoundException;
import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.UserProfileRepository;
import com.wised.helpandsettings.dtos.HelpResponse;
import com.wised.people.dtos.FolloweeAndFollowingResponse;
import com.wised.people.dtos.PeopleResponse;
import com.wised.people.dtos.ReportRequest;
import com.wised.people.dtos.UserBlockResponse;
import com.wised.people.exception.UserAlreadyFollowedException;
import com.wised.people.exception.UserNotFollowedException;
import com.wised.people.service.PeopleService;
import lombok.AllArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/people")
public class PeopleController {

    private final PeopleService peopleService;

    private final UserProfileRepository userProfileRepository;

    @PostMapping("/follow/{username}")
    public ResponseEntity<PeopleResponse> followUser(@PathVariable String username) {
        try {
            peopleService.followUser(username);
            return ResponseEntity.ok().body(PeopleResponse.builder()
                    .message("User followed successfully")
                    .build());
        } catch (UserNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(PeopleResponse.builder()
                    .error(e.getMessage())
                    .build());
        } catch (UserAlreadyFollowedException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(PeopleResponse.builder()
                    .error(e.getMessage())
                    .build());
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(PeopleResponse.builder()
                    .error("Failed to follow user")
                    .build());
        }
    }

    @PostMapping("/unfollow/{username}")
    public ResponseEntity<PeopleResponse> unfollowUser(@PathVariable String username) {
        try {
            peopleService.unfollowUser(username);
            return ResponseEntity.ok().body(PeopleResponse.builder()
                    .message("User unfollowed successfully")
                    .build());
        } catch (UserNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(PeopleResponse.builder()
                    .error(e.getMessage())
                    .build());
        } catch (UserNotFollowedException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(PeopleResponse.builder()
                    .error(e.getMessage())
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(PeopleResponse.builder()
                    .error("Failed to follow user")
                    .build());
        }
    }

    @GetMapping("/byLocation")
    public ResponseEntity<PeopleResponse> getUserProfilesByLocation() {
        try{
            PeopleResponse response = peopleService.findUserProfilesByLocation();
            return ResponseEntity.ok().body(response);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/byUniversityOrSchool")
    public ResponseEntity<PeopleResponse> getUserProfilesByUniversityOrSchool() {
        try{
            PeopleResponse response = peopleService.findUserProfilesByUniversityOrSchool();
            return ResponseEntity.ok().body(response);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/byTopCreators")
    public ResponseEntity<PeopleResponse> getTopCreators() {
        try{
            PeopleResponse response = peopleService.topCreators();
            return ResponseEntity.ok().body(response);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/followers")
    public ResponseEntity<FolloweeAndFollowingResponse> getFollowersDetails() {
        try {
            FolloweeAndFollowingResponse followers = peopleService.getFollowersList();
            return ResponseEntity.ok(followers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/following")
    public ResponseEntity<FolloweeAndFollowingResponse> getFollowingDetails() {
        try {
            FolloweeAndFollowingResponse followee = peopleService.getFollowingList();
            return ResponseEntity.ok(followee);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/report")
    public ResponseEntity<PeopleResponse> reportUser(@RequestBody ReportRequest reportRequest) {
        try {
            // Get the currently authenticated user
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userEmail = currentUser.getUsername();

            // Retrieve the user's profile from the repository
            Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

            if (!userProfileOptional.isPresent()) {
                return ResponseEntity.badRequest().body(PeopleResponse.builder()
                        .error("Invalid user")
                        .build());
            }

            UserProfile reportingUserProfile = userProfileOptional.get();
            System.out.println("started");
            PeopleResponse response = peopleService.reportUser(reportingUserProfile, reportRequest);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
        }
    }
}
