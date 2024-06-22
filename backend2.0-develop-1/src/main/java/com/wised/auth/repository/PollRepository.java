package com.wised.auth.repository;

import com.wised.auth.enums.PollType;
import com.wised.auth.model.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PollRepository extends JpaRepository<Poll, Integer> {

    Optional<Poll> findById(Integer id);

    List<Poll> findByIsActiveTrue();

    List<Poll> findByUser_Id(Integer userId);

    List<Poll> findByQuestionContaining(String partialQuestion);

    List<Poll> findByType(PollType type);

    @Query("SELECT p FROM Poll p WHERE p.isActive = true AND p.type = :pollType")
    List<Poll> findActivePollsByType(PollType pollType);

    // You can add more custom query methods here as needed
}