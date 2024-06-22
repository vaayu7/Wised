package com.wised.auth.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wised.auth.enums.InterestType;
import com.wised.auth.model.*;
import com.wised.auth.repository.UserInputAdditionRepository;
import com.wised.auth.repository.UserInterestRepository;
import com.wised.auth.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Service class for managing user profiles.
 */
@Service
@AllArgsConstructor
public class UserInterestService {

    private static final String jsonFilePath = "Interest.json";
    private final UserInterestRepository userInterestRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserInputAdditionRepository userInputAdditionRepository;


    public List<String> getUserInterests(UserProfile userProfile) {
        List<String> interests = new ArrayList<>();

        // Retrieve user interests from UserInterest table by UserProfile
        List<UserInterest> userInterests = userInterestRepository.findByUserProfile(userProfile);

        // Add interests from UserInterest table to the list
        for (UserInterest userInterest : userInterests) {
            interests.add(userInterest.getInterest());
        }

        // Retrieve user additions from UserInputAddition table by UserProfile
        List<UserInputAddition> userInputAdditions = userInputAdditionRepository.findByUserProfile(userProfile);

        // Add additions to the list
        for (UserInputAddition userInputAddition : userInputAdditions) {
            interests.add(userInputAddition.getAddition());
        }

        return interests;
    }

    @Transactional
    public void addUserInterests(UserProfile userProfile, List<String> interests, List<String> otherInterestList) {
        interests.forEach(interest -> saveUserInterest(userProfile, interest));

        otherInterestList.forEach(interest -> {
            int existingCount = userInputAdditionRepository.countByAddition(interest);
            if (existingCount + 1 >= 5) {
                saveInterestToJSON(interest);
            }
            saveUserInputAddition(userProfile, interest, existingCount + 1);
        });
    }

    private void saveUserInterest(UserProfile userProfile, String interest) {
        UserInterest userInterest = UserInterest.builder()
                .interest(interest)
                .userProfile(userProfile)
                .build();
        userInterestRepository.save(userInterest);
    }

    private void saveUserInputAddition(UserProfile userProfile, String interest, int count) {
        UserInputAddition userInterestAddition = UserInputAddition.builder()
                .type(InterestType.INTEREST)
                .addition(interest)
                .userProfile(userProfile)
                .count(count)
                .build();
        userInputAdditionRepository.save(userInterestAddition);
    }

    private void saveInterestToJSON(String interest) {
        try {
            List<String> existingInterests = readInterestsFromJson();
            existingInterests.add(interest);

            ObjectMapper mapper = new ObjectMapper();
            String interestsJson = mapper.writeValueAsString(existingInterests);

            Files.writeString(Path.of(jsonFilePath), interestsJson);
        } catch (IOException e) {
            e.printStackTrace(); // Handle or log the exception
        }
    }

    private List<String> readInterestsFromJson() throws IOException {
        if (!Files.exists(Path.of(jsonFilePath))) {
            return List.of(); // Return empty list if file does not exist
        }
        return Files.readAllLines(Path.of(jsonFilePath))
                .stream()
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeUserInterest(UserProfile userProfile, Integer interestId) {
        // Find the user interest by interestId and userId

        UserInterest userInterest = userInterestRepository.findByIdAndUserProfile(interestId, userProfile)
                .orElseThrow(() -> new IllegalArgumentException("User Interest not found with ID: " + interestId));

        // Delete the user interest
        userInterestRepository.delete(userInterest);
    }
}
