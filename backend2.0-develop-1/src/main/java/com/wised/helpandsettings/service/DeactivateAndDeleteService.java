package com.wised.helpandsettings.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.crypto.PasswordBasedDecrypter;
import com.wised.auth.dtos.ApiResponse;
import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.auth.repository.UserProfileRepository;
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
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class DeactivateAndDeleteService {

    private final DeactivateAndDeleteRepository deactivateAndDeleteRepository;

    private final ReasonRepository reasonRepository;

    private final UserRepository userRepository;

    private final LogoutService logoutService;

    private final LogoutHandler logoutHandler;

    public void deactivateAccount(UserProfile userProfile, String userPassword, DeactivateOrDeleteRequest deactivateOrDeleteRequest, Boolean isDeactivateRequest) throws PasswordNotMatchedException, MontlyDeactivateRequestExceededException {
        if(isDeactivateRequest && isAccountDeactivatedRecently(userProfile)){
            throw new MontlyDeactivateRequestExceededException("you can't deactivate more than once in a month");
        }
        if (verifyPassword(userPassword, deactivateOrDeleteRequest.getPassword())) {
            Reason reason = Reason
                    .builder()
                    .description(deactivateOrDeleteRequest.getDescription())
                    .enumReason(deactivateOrDeleteRequest.getReasonEnum())
                    .build();
            reasonRepository.save(reason);
            LocalDate currentDate = LocalDate.now();

            DeactivateAndDelete deactivateAndDelete = DeactivateAndDelete.builder()
                    .isDeactivated(true)
                    .createdAt(currentDate)
                    .reason(reason)
                    .user(userProfile)
                    .build();
            Optional<DeactivateAndDelete> lastData = deactivateAndDeleteRepository.findByUser(userProfile);
            if(lastData.isPresent()){
                DeactivateAndDelete recent = lastData.get();
                deactivateAndDeleteRepository.delete(recent);
            }
            deactivateAndDeleteRepository.save(deactivateAndDelete);
            logoutUser();
        }
        else{
            System.out.println("wrong");
           throw new PasswordNotMatchedException("wrong password");
        }
    }

    public boolean isAccountDeactivatedRecently(UserProfile userProfile) {
        Optional<DeactivateAndDelete> lastDeactivation = deactivateAndDeleteRepository.findByUser(userProfile);
        if (lastDeactivation.isPresent()) {
            LocalDate deactivationTime = lastDeactivation.get().getCreatedAt();
            LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
            return !deactivationTime.isBefore(oneMonthAgo);
        }
      return false;
    }


    public void initiateDeleteAccount(UserProfile userProfile, String userPassword, DeactivateOrDeleteRequest deactivateOrDeleteRequest) {
        try {
            deactivateAccount(userProfile, userPassword, deactivateOrDeleteRequest, false);
            Optional<DeactivateAndDelete> deactivateAndDeleteOptional = deactivateAndDeleteRepository.findByUser(userProfile);
            if (deactivateAndDeleteOptional.isPresent()) {
                DeactivateAndDelete deactivateAndDelete = deactivateAndDeleteOptional.get();
                deactivateAndDelete.setDeleted(false);
                deactivateAndDelete.setDeletionPending(true);
                deactivateAndDeleteRepository.save(deactivateAndDelete);
            }
        } catch (PasswordNotMatchedException e) {
            e.printStackTrace();
        } catch (MontlyDeactivateRequestExceededException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 1000)
    public void deleteAccount() {
        List<DeactivateAndDelete> deleteRequests = deactivateAndDeleteRepository.findByDeletionPending(true);
        for (DeactivateAndDelete deleteRequest : deleteRequests){
            LocalDate initiateDeletionDate = deleteRequest.getCreatedAt();
                LocalDate currentDate = LocalDate.now();
                LocalDate sevenDaysBefore = currentDate.minusDays(7);
                if(initiateDeletionDate.isBefore(sevenDaysBefore)){
                    UserProfile userProfile = deleteRequest.getUser();
                    boolean updateStatus=updateDeactivateStatus(userProfile);
                    boolean deleteStatus=updateDeleteStatus(userProfile);
                    if (updateStatus && deleteStatus) {
                        // Detach UserProfile from DeactivateAndDelete entity
                        deleteRequest.setUser(null);
                        // Delete UserProfile
                        userRepository.delete(userProfile.getUser());
                    }
            }
        }
    }

    public boolean updateDeactivateStatus(UserProfile userProfile) {
        Optional<DeactivateAndDelete> deactivateAndDeleteOptional = deactivateAndDeleteRepository.findByUser(userProfile);

        if (deactivateAndDeleteOptional.isPresent()) {
            DeactivateAndDelete deactivateAndDelete = deactivateAndDeleteOptional.get();
            deactivateAndDelete.setDeactivated(false);
            deactivateAndDelete.setCreatedAt(LocalDate.now());
            deactivateAndDeleteRepository.save(deactivateAndDelete);
            return true;
        }

        return false;
    }

    public boolean updateDeleteStatus(UserProfile userProfile) {
        Optional<DeactivateAndDelete> deactivateAndDeleteOptional = deactivateAndDeleteRepository.findByUser(userProfile);

        if (deactivateAndDeleteOptional.isPresent()) {
            DeactivateAndDelete deactivateAndDelete = deactivateAndDeleteOptional.get();
            deactivateAndDelete.setDeleted(true);
            deactivateAndDelete.setDeletionPending(true);
            deactivateAndDelete.setCreatedAt(LocalDate.now());
            deactivateAndDeleteRepository.save(deactivateAndDelete);
            return true;
        }

        return false;
    }

    public boolean updateDeleteStatusOnLogin (UserProfile userProfile) {
        Optional<DeactivateAndDelete> deactivateAndDeleteOptional = deactivateAndDeleteRepository.findByUser(userProfile);

        if (deactivateAndDeleteOptional.isPresent()) {
            DeactivateAndDelete deactivateAndDelete = deactivateAndDeleteOptional.get();
            deactivateAndDelete.setDeleted(false);
            deactivateAndDelete.setDeactivated(false);
            deactivateAndDelete.setDeletionPending(false);
            //deactivateAndDelete.setCreatedAt(LocalDate.now());
            deactivateAndDeleteRepository.save(deactivateAndDelete);
            return true;
        }
        return false;
    }

    private void logoutUser() {
        HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpServletResponse httpResponse = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        logoutHandler.logout(httpRequest, httpResponse, authentication);
    }

    //BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private final PasswordEncoder passwordEncoder;

   public boolean verifyPassword(String encodedPassword, String rawPassword) {
       System.out.println("verify");

       if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
           throw new IllegalArgumentException("Wrong password");
       }
       return true;
   }

    public boolean isUserProfileDeactivated(UserProfile userProfile) {
        Optional<DeactivateAndDelete> deactivateAndDeleteOptional = deactivateAndDeleteRepository.findByUser(userProfile);
        return deactivateAndDeleteOptional.map(DeactivateAndDelete::isDeactivated).orElse(false);
    }
}
