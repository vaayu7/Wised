package com.wised.auth.repository;

import com.wised.auth.model.College;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollegeRepository extends JpaRepository<College, Integer> {
    List<College> findByInstituteName(String institute);
    List<College> findByUniversityName(String university);

    List<College> findByLocation(String location);

    List<College> findByUniversityNameContainingIgnoreCase(String university);
}
