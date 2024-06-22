package com.wised.auth.repository;

import com.wised.auth.model.College;
import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository interface for managing user profiles.
 */
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {

    /**
     * Find a user profile by user ID.
     *
     * @param userId The ID of the user.
     * @return An Optional containing the user profile if found, or empty if not found.
     */
    Optional<UserProfile> findByUserId(Integer userId);

    /**
     * Find a user profile by user email.
     *
     * @param email The email address of the user.
     * @return An Optional containing the user profile if found, or empty if not found.
     */
    Optional<UserProfile> findByUserEmail(String email);

    boolean existsByUserName(String username);

    Optional<UserProfile> findByUserName(String username);

    List<UserProfile> findByUserNameContainingIgnoreCaseOrFullNameContainingIgnoreCase(String query, String query1);

    List<UserProfile> findByBioContainingIgnoreCase(String query);


    //UserProfile findByUser(User user);

    // You can add custom query methods here if needed
}

