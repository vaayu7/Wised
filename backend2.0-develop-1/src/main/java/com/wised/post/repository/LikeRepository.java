package com.wised.post.repository;

import com.wised.auth.model.UserProfile;
import com.wised.post.enums.LikeType;
import com.wised.post.model.Like;
import com.wised.post.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;

public interface LikeRepository extends JpaRepository<Like, Integer> {
    Like findByUserAndPost(UserProfile user, Post post);

    int countByPostAndType(Post post, LikeType like);

    @Query("SELECT COUNT(l) FROM Like l WHERE l.post = :post AND l.type = :liketype AND l.updatedAt >= :startDateAsDate AND l.updatedAt <= :currentDateAsDate")
    int countByPostAndTypeAndDateRange(@Param("post")Post post, @Param("liketype")LikeType likeType, @Param("startDateAsDate")Date startDateAsDate, @Param("currentDateAsDate")Date currentDateAsDate);
}