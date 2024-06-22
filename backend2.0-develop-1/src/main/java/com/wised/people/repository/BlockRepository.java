package com.wised.people.repository;

import com.wised.auth.model.UserProfile;
import com.wised.people.model.Block;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockRepository extends JpaRepository<Block, Integer> {

    boolean existsByBlockerAndBlocked(UserProfile blockerUserProfile, UserProfile blockedUserProfile);

    Block findByBlockerAndBlocked(UserProfile blocker, UserProfile blocked);
}
