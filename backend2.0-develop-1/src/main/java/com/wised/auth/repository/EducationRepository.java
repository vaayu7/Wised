package com.wised.auth.repository;

import com.wised.auth.model.College;
import com.wised.auth.model.Education;
import com.wised.auth.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EducationRepository extends JpaRepository<Education, Integer> {

    Optional<List<Education>> findByUser(UserProfile user);

    List<Education> findBySpecializationStreamAndCurrentSemester(String stream, Integer semester);

    List<Education> findBySpecializationStream(String stream);


    //Optional<List<Education>> findByUser(UserProfile user);
}