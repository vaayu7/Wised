package com.wised.auth.service;

import com.wised.auth.mappers.UserProfileMapper;
import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.auth.model.UserProfileData;
import com.wised.auth.repository.UserProfileRepository;
import com.wised.auth.dtos.UserProfileRequest;
import com.wised.auth.dtos.UserProfileResponse;
import com.wised.auth.exception.UserNotFoundException;
import com.wised.people.repository.BlockRepository;
import com.wised.people.repository.FollowRepository;
import com.wised.people.service.BlockService;
import com.wised.post.repository.LikeRepository;
import com.wised.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserProfileServiceTest {

    @InjectMocks
    private UserProfileService userProfileService;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserProfileMapper userProfileMapper;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private BlockService blockService;

    private UserProfile userProfile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userProfile = new UserProfile();
        userProfile.setId(1);
    }

    @Test
    void testCreateProfileForUser() {
        User user = new User();
        user.setFullName("John Doe");

        UserProfile expectedProfile = UserProfile.builder()
                .fullName("John Doe")
                .build();
        expectedProfile.setUser(user);

        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(expectedProfile);

        UserProfile createdProfile = userProfileService.createProfileForUser(user);

        assertNotNull(createdProfile);
        assertEquals("John Doe", createdProfile.getFullName());
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void testUpdateProfileFields() {
        UserProfileRequest request = new UserProfileRequest();
        request.setUserName("newusername");

        when(userProfileRepository.existsByUserName("newusername")).thenReturn(false);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);
        when(userProfileMapper.mapUserProfileToUserProfileData(any(UserProfile.class))).thenReturn(new UserProfileData());

        UserProfileResponse response = userProfileService.updateProfileFields(userProfile, request);

        assertTrue(response.isSuccess());
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void testUpdateProfileFieldsUserNameTaken() {
        UserProfileRequest request = new UserProfileRequest();
        request.setUserName("takenusername");

        when(userProfileRepository.existsByUserName("takenusername")).thenReturn(true);

        UserProfileResponse response = userProfileService.updateProfileFields(userProfile, request);

        assertFalse(response.isSuccess());
        assertEquals("Username is already taken", response.getMessage());
        verify(userProfileRepository, times(0)).save(any(UserProfile.class));
    }

    @Test
    void testIsUserNameAvailable() {
        when(userProfileRepository.existsByUserName("availableusername")).thenReturn(false);

        boolean isAvailable = userProfileService.isUserNameAvailable("availableusername");

        assertTrue(isAvailable);
        verify(userProfileRepository, times(1)).existsByUserName("availableusername");
    }

    @Test
    void testSuggestUniqueUsernames() {
        when(userProfileRepository.existsByUserName(anyString())).thenReturn(false);

        List<String> suggestions = userProfileService.suggestUniqueUsernames("baseusername");

        assertEquals(2, suggestions.size());
    }

    @Test
    void testGetProfile() {
        when(followRepository.countByFollower(userProfile)).thenReturn(10);
        when(followRepository.countByFollowee(userProfile)).thenReturn(20);
        when(userProfileMapper.mapUserProfileToUserProfileData(any(UserProfile.class), anyInt(), anyInt(), anyBoolean())).thenReturn(new UserProfileData());

        UserProfileData profileData = userProfileService.getProfile(userProfile);

        assertNotNull(profileData);
        verify(followRepository, times(1)).countByFollower(userProfile);
        verify(followRepository, times(1)).countByFollowee(userProfile);
        verify(userProfileMapper, times(1)).mapUserProfileToUserProfileData(any(UserProfile.class), anyInt(), anyInt(), anyBoolean());
    }

    @Test
    void testGetProfileByUsername() {
        UserProfile otherUserProfile = new UserProfile();
        otherUserProfile.setId(2);
        otherUserProfile.setUserName("otheruser");

        when(userProfileRepository.findByUserName("otheruser")).thenReturn(Optional.of(otherUserProfile));
        when(followRepository.countByFollower(otherUserProfile)).thenReturn(5);
        when(followRepository.countByFollowee(otherUserProfile)).thenReturn(15);
        when(blockService.isUserBlocked(any(UserProfile.class), any(UserProfile.class))).thenReturn(false);
        when(userProfileMapper.mapUserProfileToUserProfileData(any(UserProfile.class), anyInt(), anyInt(), anyBoolean())).thenReturn(new UserProfileData());

        UserProfileData profileData = userProfileService.getProfile(userProfile, "otheruser");

        assertNotNull(profileData);
        verify(userProfileRepository, times(1)).findByUserName("otheruser");
        verify(followRepository, times(1)).countByFollower(otherUserProfile);
        verify(followRepository, times(1)).countByFollowee(otherUserProfile);
        verify(blockService, times(1)).isUserBlocked(any(UserProfile.class), any(UserProfile.class));
        verify(userProfileMapper, times(1)).mapUserProfileToUserProfileData(any(UserProfile.class), anyInt(), anyInt(), anyBoolean());
    }

    @Test
    void testGetProfileByUsernameNotFound() {
        when(userProfileRepository.findByUserName("nonexistentuser")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userProfileService.getProfile(userProfile, "nonexistentuser");
        });

        assertEquals("User profile not found for username: nonexistentuser", exception.getMessage());
        verify(userProfileRepository, times(1)).findByUserName("nonexistentuser");
    }
}

