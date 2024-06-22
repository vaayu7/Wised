package com.wised.people.repository;

import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.people.model.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Integer> {

    boolean existsByFollowerAndFollowee(UserProfile follower, UserProfile followee);

    void deleteByFollowerAndFollowee(UserProfile follower, UserProfile followee);

    int countByFollowee(UserProfile userProfile);

    int countByFollower(UserProfile userProfile);

    Optional<List<Follow>> findByFollowee(UserProfile followee);

    Optional<List<Follow>> findByFollower(UserProfile follower);
}
