package com.wised.auth.repository;

import com.wised.auth.model.Poll;
import com.wised.auth.model.User;
import com.wised.auth.model.UsertoPollMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UsertoPollMapperRepository extends JpaRepository<UsertoPollMapper, Integer> {

    // You can add custom query methods here as needed



    // Example of a custom query method to find mappings by user ID

    List<UsertoPollMapper> findByUser_Id(Integer userId);



    // Example of a custom query method to find mappings by poll ID

    List<UsertoPollMapper> findByPoll_Id(Integer pollId);

    Optional<UsertoPollMapper> findByUserAndPoll(User user, Poll poll);

}
