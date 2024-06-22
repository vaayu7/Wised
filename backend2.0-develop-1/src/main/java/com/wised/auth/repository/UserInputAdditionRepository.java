package com.wised.auth.repository;

import com.wised.auth.model.UserInputAddition;
import com.wised.auth.model.UserInterest;
import com.wised.auth.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserInputAdditionRepository extends JpaRepository<UserInputAddition, Integer> {

    /**
     * Count the number of UserInterests by interest.
     *
     * @param interest The interest to count.
     * @return The count of UserInterests for the specified interest.
     */
    int countByAddition(String interest);

    List<UserInputAddition> findByUserProfile(UserProfile userProfile);

}
