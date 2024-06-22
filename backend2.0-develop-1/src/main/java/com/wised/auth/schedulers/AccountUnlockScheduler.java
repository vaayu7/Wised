package com.wised.auth.schedulers;

import com.wised.auth.model.User;
import com.wised.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.List;


/**
 * This component is responsible for periodically unlocking user accounts that have been locked
 * due to too many failed login attempts.
 */
@Component
public class AccountUnlockScheduler {



    @Autowired
    private UserRepository userRepository;

    /**
     * Scheduled method that runs periodically to unlock user accounts with expired lock times.
     * The fixed rate is set to 120,000 milliseconds (2 minutes) by default.
     */
    @Scheduled(fixedRate = 120000)
    public void unlockAccounts() {
        // Get the current time
        Calendar currentTime = Calendar.getInstance();
        System.out.print("running unlockAccounts");

        // Query the database for locked accounts with lock times that have expired
        List<User> lockedUsers = userRepository.findLockedUsersWithExpiredLockTime(currentTime.getTime());
        System.out.print("running unlockAccounts");
        for (User user : lockedUsers) {
            System.out.print("unlockAccounts" + user.getEmail());

            // Reset account lock status, lock time, and failed attempt count
            user.setAccountLocked(false);
            user.setLockTime(null);
            user.setFailedAttempt(0);

            // Save the updated user information
            userRepository.save(user);
        }
    }
}

