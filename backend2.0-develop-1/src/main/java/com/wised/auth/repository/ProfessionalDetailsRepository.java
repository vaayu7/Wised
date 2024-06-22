package com.wised.auth.repository;

import com.wised.auth.model.Education;
import com.wised.auth.model.ProfessionalDetails;
import com.wised.auth.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfessionalDetailsRepository extends JpaRepository<ProfessionalDetails, Integer> {


    Optional<ProfessionalDetails> findByIdAndUserProfile(int id, UserProfile userProfile);
    Optional<List<ProfessionalDetails>> findByUserProfile(UserProfile userProfile);
    List<ProfessionalDetails> findByLocation(String location);
}
