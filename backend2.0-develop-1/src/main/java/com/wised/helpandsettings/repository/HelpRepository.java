package com.wised.helpandsettings.repository;

import com.wised.auth.model.UserProfile;
import com.wised.helpandsettings.enums.HelpStatus;
import com.wised.helpandsettings.model.Help;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HelpRepository extends JpaRepository<Help, Integer> {

    List<Help> findByHelpStatus(HelpStatus status);

    List<Help> findByUserProfile(UserProfile userProfile);

    List<Help> findByEmail(String email);

    List<Help> findBySubject(String subject);

    List<Help> findByIssue(String issue);

    List<Help> findByDescription(String description);
}