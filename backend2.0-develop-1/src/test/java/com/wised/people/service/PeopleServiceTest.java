package com.wised.people.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.wised.auth.exception.UserNotFoundException;
import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.UserProfileRepository;
import com.wised.auth.repository.UserRepository;
import com.wised.people.dtos.FolloweeAndFollowingResponse;
import com.wised.people.dtos.PeopleResponse;
import com.wised.people.dtos.ReportRequest;
import com.wised.people.enums.ReportReason;
import com.wised.people.exception.UserAlreadyFollowedException;
import com.wised.people.exception.UserNotFollowedException;
import com.wised.people.model.Follow;
import com.wised.people.repository.FollowRepository;
import com.wised.people.repository.ReportUserRepository;
import com.wised.post.repository.LikeRepository;
import com.wised.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

class PeopleServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private FollowRepository followRepository;
    @Mock
    private ReportUserRepository reportUserRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private PeopleService peopleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void followUser_UserAlreadyFollowedException() {
        UserProfile follower = new UserProfile();
        UserProfile followee = new UserProfile();
        when(userProfileRepository.findByUserName(anyString())).thenReturn(Optional.of(followee));
        when(followRepository.existsByFollowerAndFollowee(follower, followee)).thenReturn(true);
        setSecurityContext(follower);

        assertThrows(UserAlreadyFollowedException.class, () -> peopleService.followUser("username"));
    }

    @Test
    void followUser_UserNotFoundException() {
        UserProfile follower = new UserProfile();
        when(userProfileRepository.findByUserName(anyString())).thenReturn(Optional.empty());
        setSecurityContext(follower);

        assertThrows(UserNotFoundException.class, () -> peopleService.followUser("username"));
    }

    @Test
    void followUser_Success() throws UserAlreadyFollowedException, UserNotFoundException {
        UserProfile follower = new UserProfile();
        UserProfile followee = new UserProfile();
        when(userProfileRepository.findByUserName(anyString())).thenReturn(Optional.of(followee));
        when(followRepository.existsByFollowerAndFollowee(follower, followee)).thenReturn(false);
        setSecurityContext(follower);

        peopleService.followUser("username");
        verify(followRepository, times(1)).save(any(Follow.class));
    }

    @Test
    void unfollowUser_UserNotFollowedException() {
        UserProfile follower = new UserProfile();
        UserProfile followee = new UserProfile();
        when(userProfileRepository.findByUserName(anyString())).thenReturn(Optional.of(followee));
        when(followRepository.existsByFollowerAndFollowee(follower, followee)).thenReturn(false);
        setSecurityContext(follower);

        assertThrows(UserNotFollowedException.class, () -> peopleService.unfollowUser("username"));
    }

    @Test
    void unfollowUser_UserNotFoundException() {
        UserProfile follower = new UserProfile();
        when(userProfileRepository.findByUserName(anyString())).thenReturn(Optional.empty());
        setSecurityContext(follower);

        assertThrows(UserNotFoundException.class, () -> peopleService.unfollowUser("username"));
    }

    @Test
    void unfollowUser_Success() throws UserNotFollowedException, UserNotFoundException {
        UserProfile follower = new UserProfile();
        UserProfile followee = new UserProfile();
        when(userProfileRepository.findByUserName(anyString())).thenReturn(Optional.of(followee));
        when(followRepository.existsByFollowerAndFollowee(follower, followee)).thenReturn(true);
        setSecurityContext(follower);

        peopleService.unfollowUser("username");
        verify(followRepository, times(1)).deleteByFollowerAndFollowee(follower, followee);
    }

    @Test
    void findUserProfilesByLocation_NoLocation() {
        when(userProfileRepository.findByUserEmail(anyString())).thenReturn(Optional.of(new UserProfile()));
        setSecurityContext(new UserProfile());

        PeopleResponse response = peopleService.findUserProfilesByLocation();
        assertEquals("No Location found", response.getError());
    }

    @Test
    void getFollowersList_NoFollowers() {
        UserProfile followee = new UserProfile();
        when(followRepository.findByFollowee(any(UserProfile.class))).thenReturn(Optional.empty());
        setSecurityContext(followee);

        FolloweeAndFollowingResponse response = peopleService.getFollowersList();
        assertEquals("No Followers", response.getError());
    }

    @Test
    void getFollowersList_Success() throws UserNotFoundException {
        UserProfile followee = new UserProfile();
        Follow follow = new Follow();
        follow.setFollower(new UserProfile());
        List<Follow> followers = Collections.singletonList(follow);
        when(followRepository.findByFollowee(any(UserProfile.class))).thenReturn(Optional.of(followers));
        setSecurityContext(followee);

        FolloweeAndFollowingResponse response = peopleService.getFollowersList();
        assertTrue(response.isSuccess());
    }

    @Test
    void reportUser_UserNotFoundException() {
        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setReportedUserName("unknownUser");
        when(userProfileRepository.findByUserName("unknownUser")).thenReturn(Optional.empty());

        UserProfile reportingUser = new UserProfile();
        PeopleResponse response = peopleService.reportUser(reportingUser, reportRequest);
        assertEquals("Failed to Report the User", response.getError());
    }

    @Test
    void reportUser_Success() {
        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setReportedUserName("reportedUser");
        reportRequest.setReportReason(ReportReason.SPAM);
        UserProfile reportedUser = new UserProfile();
        when(userProfileRepository.findByUserName("reportedUser")).thenReturn(Optional.of(reportedUser));

        UserProfile reportingUser = new UserProfile();
        PeopleResponse response = peopleService.reportUser(reportingUser, reportRequest);
        assertEquals("User Successfully Reported", response.getMessage());
        verify(reportUserRepository, times(1)).save(any());
    }

    private void setSecurityContext(UserProfile userProfile) {
        User user = new User();
        user.setUserProfile(userProfile);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }
}
