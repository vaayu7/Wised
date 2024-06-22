package com.wised.auth.repository;

import com.wised.auth.model.School;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.List;

public interface SchoolRepository extends JpaRepository<School, Integer> {
    List<School> findBySchoolName(String schoolName);

    List<School> findByLocation(String location);
}
