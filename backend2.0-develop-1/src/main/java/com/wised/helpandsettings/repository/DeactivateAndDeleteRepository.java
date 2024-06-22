package com.wised.helpandsettings.repository;

import com.wised.auth.model.UserProfile;
import com.wised.helpandsettings.model.DeactivateAndDelete;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeactivateAndDeleteRepository extends JpaRepository<DeactivateAndDelete, Integer> {

    Optional<DeactivateAndDelete> findByUser(UserProfile user);

    List<DeactivateAndDelete> findByDeletionPending(Boolean deletionPending);

    List<UserProfile> findByIsDeactivated(boolean b);
}
