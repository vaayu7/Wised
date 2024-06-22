package com.wised.auth.controller;


import com.wised.auth.dtos.PollDto;
import com.wised.auth.dtos.PollRequest;
import com.wised.auth.dtos.PollResponse;
import com.wised.auth.mappers.PollMapper;
import com.wised.auth.model.Poll;
import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.UserProfileRepository;
import com.wised.auth.service.PollService;
import com.wised.auth.service.UsertoPollMapperService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * The `TestingController` class provides endpoints for testing the functionality of the application.
 * It includes endpoints for checking if the application is running and testing secured routes.
 */

@RestController
@RequestMapping("/api/v1/poll")
@AllArgsConstructor
public class PollController {


    private final PollService pollService;


    private final UsertoPollMapperService usertoPollMapperService;


    private final UserProfileRepository userProfileRepository;


    private final PollMapper pollMapper;



    @PostMapping("/create")
    public ResponseEntity<PollResponse> createPoll(@RequestBody PollRequest pollRequest) {


        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userEmail = currentUser.getUsername();
//
        // Retrieve the user's profile from the repository
        Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

        if (!userProfileOptional.isPresent()) {
            PollResponse pollResponse = PollResponse
                    .builder()
                    .success(false)
                    .error("User profile not found")
                    .message("User profile not found. Please provide a valid user.")
                    .response(null)
                    .build();
            return new ResponseEntity<>(pollResponse, HttpStatus.NOT_FOUND);
        } else {

            UserProfile userProfile = userProfileOptional.get();

            try {

                Poll poll = pollService.createPoll(userProfile, pollRequest);

                PollDto pollDto = pollMapper.toDto(poll);

                PollResponse pollResponse = PollResponse
                        .builder()
                        .success(true)
                        .error(null)
                        .message("Poll successfully created")
                        .response(pollDto)
                        .build();

                return ResponseEntity.ok(pollResponse); // Successful created, HTTP 200 OK


            } catch (Exception e) {

                PollResponse pollResponse = PollResponse
                        .builder()
                        .success(false)
                        .error(e.getMessage())
                        .message("failed to create the poll")
                        .response(null)
                        .build();

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pollResponse);

            }
        }
    }

    @GetMapping("/inactive/{pollId}")
    public ResponseEntity<PollResponse> inactivePoll(@PathVariable Integer pollId) {

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userEmail = currentUser.getUsername();
//
        // Retrieve the user's profile from the repository
        Optional<UserProfile> userProfileOptional = userProfileRepository.findByUserEmail(userEmail);

        if (!userProfileOptional.isPresent()) {
            PollResponse pollResponse = PollResponse
                    .builder()
                    .success(false)
                    .error("User profile not found")
                    .message("User profile not found. Please provide a valid user.")
                    .response(null)
                    .build();
            return new ResponseEntity<>(pollResponse, HttpStatus.NOT_FOUND);
        } else {

            UserProfile userProfile = userProfileOptional.get();

            UserProfile ownerUserProfile = pollService.getOwnerByPollId(pollId);

            System.out.println(ownerUserProfile + " owner");

            if (userEmail.equals(ownerUserProfile.getUser().getEmail())) {

                System.out.println(" macthed");

                boolean response = pollService.updateStatus(pollId);

                if (response) {
                    PollResponse pollResponse = PollResponse
                            .builder()
                            .success(true)
                            .error(null)
                            .message("Poll successfully stopped")
                            .build();

                    return ResponseEntity.ok(pollResponse);

                } else {

                    PollResponse pollResponse = PollResponse
                            .builder()
                            .success(false)
                            .error("error stopping the poll")
                            .message("please provide the correct pollId")
                            .build();

                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pollResponse);

                }

            } else {

                PollResponse pollResponse = PollResponse
                        .builder()
                        .success(false)
                        .error("bad request")
                        .message("user is not permitted to stop the quiz")
                        .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pollResponse);

            }

        }
    }
}