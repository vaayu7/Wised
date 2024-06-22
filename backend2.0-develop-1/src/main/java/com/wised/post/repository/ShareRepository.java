package com.wised.post.repository;

import com.wised.post.model.Post;
import com.wised.post.model.Share;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ShareRepository extends JpaRepository<Share, Integer> {


    Optional<List<Share>> findByPost(Post post);

    int countDistinctByPost(Post post);

    @Query("SELECT COUNT(DISTINCT s.user) FROM Share s WHERE s.post = :post AND s.createdAt BETWEEN :startDate AND :endDate")
    int countUniqueUsersByPostAndDateRange(@Param("post") Post post, @Param("startDate") Date startDate, @Param("endDate") Date endDate);


}
