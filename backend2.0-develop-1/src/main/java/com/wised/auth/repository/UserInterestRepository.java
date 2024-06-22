package com.wised.auth.repository;

import com.wised.auth.model.Token;
import com.wised.auth.model.UserInterest;
import com.wised.auth.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
public interface UserInterestRepository extends JpaRepository<UserInterest, Integer> {

    /**
     * Find all user interests by UserProfile.
     *
     * @param userProfile The UserProfile whose interests are to be retrieved.
     * @return A list of UserInterests associated with the given UserProfile.
     */
    List<UserInterest> findByUserProfile(UserProfile userProfile);

    /**
     * Find a user interest by its ID and UserProfile.
     *
     * @param id         The ID of the interest.
     * @param userProfile The UserProfile associated with the interest.
     * @return An Optional containing the UserInterest if found, or empty if not found.
     */
    Optional<UserInterest> findByIdAndUserProfile(Integer id, UserProfile userProfile);

    // You can add more custom query methods here if needed
}
