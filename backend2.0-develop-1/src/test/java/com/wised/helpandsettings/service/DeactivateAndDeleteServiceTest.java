package com.wised.helpandsettings.service;

import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.UserRepository;
import com.wised.auth.service.LogoutService;
import com.wised.helpandsettings.dtos.DeactivateOrDeleteRequest;
import com.wised.helpandsettings.exception.MontlyDeactivateRequestExceededException;
import com.wised.helpandsettings.exception.PasswordNotMatchedException;
import com.wised.helpandsettings.model.DeactivateAndDelete;
import com.wised.helpandsettings.model.Reason;
import com.wised.helpandsettings.repository.DeactivateAndDeleteRepository;
import com.wised.helpandsettings.repository.ReasonRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutHandler;

//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeactivateAndDeleteServiceTest {

    @Mock
    private DeactivateAndDeleteRepository deactivateAndDeleteRepository;

    @Mock
    private ReasonRepository reasonRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LogoutService logoutService;

    @Mock
    private LogoutHandler logoutHandler;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DeactivateAndDeleteService deactivateAndDeleteService;

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
    void deactivateAccount_PasswordNotMatched() {
        UserProfile userProfile = new UserProfile();
        DeactivateOrDeleteRequest request = new DeactivateOrDeleteRequest();
        request.setPassword("wrongPassword");

        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(PasswordNotMatchedException.class, () ->
                deactivateAndDeleteService.deactivateAccount(userProfile, "userPassword", request, true));
    }

    @Test
    void deactivateAccount_ExceededMonthlyRequest() {
        UserProfile userProfile = new UserProfile();
        DeactivateOrDeleteRequest request = new DeactivateOrDeleteRequest();
        request.setPassword("password");

        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(deactivateAndDeleteRepository.findByUser(userProfile)).thenReturn(Optional.of(
                new DeactivateAndDelete(null, true, LocalDate.now().minusDays(10), null, false, false, userProfile)
        ));

        assertThrows(MontlyDeactivateRequestExceededException.class, () ->
                deactivateAndDeleteService.deactivateAccount(userProfile, "userPassword", request, true));
    }

    @Test
    void deactivateAccount_Success() throws Exception {
        UserProfile userProfile = new UserProfile();
        DeactivateOrDeleteRequest request = new DeactivateOrDeleteRequest();
        request.setPassword("password");

        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(deactivateAndDeleteRepository.findByUser(userProfile)).thenReturn(Optional.empty());

        deactivateAndDeleteService.deactivateAccount(userProfile, "userPassword", request, true);

        verify(reasonRepository, times(1)).save(any(Reason.class));
        verify(deactivateAndDeleteRepository, times(1)).save(any(DeactivateAndDelete.class));
        verify(logoutHandler, times(1)).logout(any(HttpServletRequest.class), any(HttpServletResponse.class), any(Authentication.class));
    }

    @Test
    void isAccountDeactivatedRecently_DeactivatedRecently() {
        UserProfile userProfile = new UserProfile();
        DeactivateAndDelete deactivated = new DeactivateAndDelete(null, true, LocalDate.now().minusDays(10), null, false, false, userProfile);

        when(deactivateAndDeleteRepository.findByUser(userProfile)).thenReturn(Optional.of(deactivated));

        boolean result = deactivateAndDeleteService.isAccountDeactivatedRecently(userProfile);

        assertTrue(result);
    }

    @Test
    void isAccountDeactivatedRecently_NotDeactivatedRecently() {
        UserProfile userProfile = new UserProfile();
        DeactivateAndDelete deactivated = new DeactivateAndDelete(null, true, LocalDate.now().minusMonths(2), null, false, false, userProfile);

        when(deactivateAndDeleteRepository.findByUser(userProfile)).thenReturn(Optional.of(deactivated));

        boolean result = deactivateAndDeleteService.isAccountDeactivatedRecently(userProfile);

        assertFalse(result);
    }

    @Test
    void initiateDeleteAccount_Success() {
        UserProfile userProfile = new UserProfile();
        DeactivateOrDeleteRequest request = new DeactivateOrDeleteRequest();
        request.setPassword("password");

        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(deactivateAndDeleteRepository.findByUser(userProfile)).thenReturn(Optional.of(new DeactivateAndDelete()));

        deactivateAndDeleteService.initiateDeleteAccount(userProfile, "userPassword", request);

        verify(deactivateAndDeleteRepository, times(1)).save(any(DeactivateAndDelete.class));
    }

    @Test
    void deleteAccount_Success() {
        UserProfile userProfile = new UserProfile();
        DeactivateAndDelete deactivated = new DeactivateAndDelete(null, true, LocalDate.now().minusDays(10), null, false, true, userProfile);

        when(deactivateAndDeleteRepository.findByDeletionPending(true)).thenReturn(List.of(deactivated));
        when(deactivateAndDeleteService.updateDeactivateStatus(userProfile)).thenReturn(true);
        when(deactivateAndDeleteService.updateDeleteStatus(userProfile)).thenReturn(true);

        deactivateAndDeleteService.deleteAccount();

        verify(userRepository, times(1)).delete(any(User.class));
    }

    @Test
    void updateDeactivateStatus_Success() {
        UserProfile userProfile = new UserProfile();
        DeactivateAndDelete deactivated = new DeactivateAndDelete(null, true, LocalDate.now(), null, false, false, userProfile);

        when(deactivateAndDeleteRepository.findByUser(userProfile)).thenReturn(Optional.of(deactivated));

        boolean result = deactivateAndDeleteService.updateDeactivateStatus(userProfile);

        assertTrue(result);
        verify(deactivateAndDeleteRepository, times(1)).save(deactivated);
    }

    @Test
    void updateDeleteStatus_Success() {
        UserProfile userProfile = new UserProfile();
        DeactivateAndDelete deactivated = new DeactivateAndDelete(null, true, LocalDate.now(), null, false, false, userProfile);

        when(deactivateAndDeleteRepository.findByUser(userProfile)).thenReturn(Optional.of(deactivated));

        boolean result = deactivateAndDeleteService.updateDeleteStatus(userProfile);

        assertTrue(result);
        verify(deactivateAndDeleteRepository, times(1)).save(deactivated);
    }

    @Test
    void updateDeleteStatusOnLogin_Success() {
        UserProfile userProfile = new UserProfile();
        DeactivateAndDelete deactivated = new DeactivateAndDelete();

        when(deactivateAndDeleteRepository.findByUser(userProfile)).thenReturn(Optional.of(deactivated));

        boolean result = deactivateAndDeleteService.updateDeleteStatusOnLogin(userProfile);

        assertTrue(result);
        verify(deactivateAndDeleteRepository, times(1)).save(deactivated);
    }

    @Test
    void isUserProfileDeactivated_Success() {
        UserProfile userProfile = new UserProfile();
        DeactivateAndDelete deactivated = new DeactivateAndDelete();
        deactivated.setDeactivated(true);

        when(deactivateAndDeleteRepository.findByUser(userProfile)).thenReturn(Optional.of(deactivated));

        boolean result = deactivateAndDeleteService.isUserProfileDeactivated(userProfile);

        assertTrue(result);
    }
}
