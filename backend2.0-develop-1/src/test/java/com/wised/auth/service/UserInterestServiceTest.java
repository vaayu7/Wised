package com.wised.auth.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.wised.auth.model.UserInputAddition;
import com.wised.auth.model.UserInterest;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.UserInputAdditionRepository;
import com.wised.auth.repository.UserInterestRepository;
import com.wised.auth.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserInterestServiceTest {

    @InjectMocks
    private UserInterestService userInterestService;

    @Mock
    private UserInterestRepository userInterestRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserInputAdditionRepository userInputAdditionRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Path path;

    private UserProfile userProfile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userProfile = new UserProfile();
        userProfile.setId(1);
    }

    @Test
    void testGetUserInterests() {
        UserInterest userInterest = UserInterest.builder()
                .interest("Interest1")
                .userProfile(userProfile)
                .build();

        UserInputAddition userInputAddition = UserInputAddition.builder()
                .addition("Addition1")
                .userProfile(userProfile)
                .build();

        when(userInterestRepository.findByUserProfile(userProfile)).thenReturn(List.of(userInterest));
        when(userInputAdditionRepository.findByUserProfile(userProfile)).thenReturn(List.of(userInputAddition));

        List<String> interests = userInterestService.getUserInterests(userProfile);

        assertEquals(2, interests.size());
        assertTrue(interests.contains("Interest1"));
        assertTrue(interests.contains("Addition1"));
    }

    @Test
    void testAddUserInterests() throws IOException {
        List<String> interests = List.of("Interest1", "Interest2");
        List<String> otherInterestList = List.of("OtherInterest1", "OtherInterest2");

        when(userInputAdditionRepository.countByAddition(anyString())).thenReturn(4);
        doNothing().when(userInputAdditionRepository).save(any(UserInputAddition.class));
        doNothing().when(userInterestRepository).save(any(UserInterest.class));
        doNothing().when(Files.class);
        doNothing().when(objectMapper);

        userInterestService.addUserInterests(userProfile, interests, otherInterestList);

        verify(userInterestRepository, times(interests.size())).save(any(UserInterest.class));
        verify(userInputAdditionRepository, times(otherInterestList.size())).save(any(UserInputAddition.class));
    }

    @Test
    void testRemoveUserInterest() {
        UserInterest userInterest = UserInterest.builder()
                .id(1)
                .userProfile(userProfile)
                .interest("Interest1")
                .build();

        when(userInterestRepository.findByIdAndUserProfile(1, userProfile)).thenReturn(Optional.of(userInterest));
        doNothing().when(userInterestRepository).delete(userInterest);

        userInterestService.removeUserInterest(userProfile, 1);

        verify(userInterestRepository, times(1)).delete(userInterest);
    }

    @Test
    void testRemoveUserInterestNotFound() {
        when(userInterestRepository.findByIdAndUserProfile(1, userProfile)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userInterestService.removeUserInterest(userProfile, 1);
        });

        assertEquals("User Interest not found with ID: 1", exception.getMessage());
    }
}

