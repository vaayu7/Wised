package com.wised.people.service;
import com.wised.auth.exception.UserNotFoundException;
import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.UserProfileRepository;
import com.wised.people.exception.UserBlockedException;
import com.wised.people.exception.UserNotFollowedException;
import com.wised.people.model.Block;
import com.wised.people.repository.BlockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BlockServiceTest {

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private PeopleService peopleService;

    @InjectMocks
    private BlockService blockService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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

    @Test
    void isUserBlocked_UserBlocked() {
        UserProfile blocker = new UserProfile();
        UserProfile blocked = new UserProfile();

        when(blockRepository.existsByBlockerAndBlocked(blocker, blocked)).thenReturn(true);

        boolean result = blockService.isUserBlocked(blocker, blocked);

        assertTrue(result);
    }

    @Test
    void isUserBlocked_UserNotBlocked() {
        UserProfile blocker = new UserProfile();
        UserProfile blocked = new UserProfile();

        when(blockRepository.existsByBlockerAndBlocked(blocker, blocked)).thenReturn(false);

        boolean result = blockService.isUserBlocked(blocker, blocked);

        assertFalse(result);
    }

    @Test
    void blockUser_UserNotFound() {
        UserProfile currentUserProfile = new UserProfile();
        setSecurityContext(currentUserProfile);

        String username = "nonexistent";

        when(userProfileRepository.findByUserName(username)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> blockService.blockUser(username));
    }

    @Test
    void blockUser_UserAlreadyBlocked() {
        UserProfile currentUserProfile = new UserProfile();
        UserProfile blockedUserProfile = new UserProfile();
        setSecurityContext(currentUserProfile);

        String username = "existingUser";

        when(userProfileRepository.findByUserName(username)).thenReturn(Optional.of(blockedUserProfile));
        when(blockRepository.existsByBlockerAndBlocked(currentUserProfile, blockedUserProfile)).thenReturn(true);

        assertThrows(UserBlockedException.class, () -> blockService.blockUser(username));
    }

    @Test
    void blockUser_Success() throws Exception, UserBlockedException, UserNotFollowedException {
        UserProfile currentUserProfile = new UserProfile();
        UserProfile blockedUserProfile = new UserProfile();
        setSecurityContext(currentUserProfile);

        String username = "existingUser";

        when(userProfileRepository.findByUserName(username)).thenReturn(Optional.of(blockedUserProfile));
        when(blockRepository.existsByBlockerAndBlocked(currentUserProfile, blockedUserProfile)).thenReturn(false);

        blockService.blockUser(username);

        verify(blockRepository, times(1)).save(any(Block.class));
        verify(peopleService, times(1)).unfollowUser(username);
    }

    @Test
    void unblockUser_UserNotFound() {
        UserProfile currentUserProfile = new UserProfile();
        setSecurityContext(currentUserProfile);

        String username = "nonexistent";

        when(userProfileRepository.findByUserName(username)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> blockService.unblockUser(username));
    }

    @Test
    void unblockUser_BlockNotFound() {
        UserProfile currentUserProfile = new UserProfile();
        UserProfile blockedUserProfile = new UserProfile();
        setSecurityContext(currentUserProfile);

        String username = "existingUser";

        when(userProfileRepository.findByUserName(username)).thenReturn(Optional.of(blockedUserProfile));
        when(blockRepository.findByBlockerAndBlocked(currentUserProfile, blockedUserProfile)).thenReturn(null);

        assertThrows(UserBlockedException.class, () -> blockService.unblockUser(username));
    }

    @Test
    void unblockUser_Success() throws Exception, UserBlockedException {
        UserProfile currentUserProfile = new UserProfile();
        UserProfile blockedUserProfile = new UserProfile();
        setSecurityContext(currentUserProfile);

        String username = "existingUser";
        Block block = new Block();

        when(userProfileRepository.findByUserName(username)).thenReturn(Optional.of(blockedUserProfile));
        when(blockRepository.findByBlockerAndBlocked(currentUserProfile, blockedUserProfile)).thenReturn(block);

        blockService.unblockUser(username);

        verify(blockRepository, times(1)).delete(block);
    }
}

