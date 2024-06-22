package com.wised.post.repository;

import com.wised.auth.model.UserProfile;
import com.wised.post.enums.PostType;
import com.wised.post.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {


    List<Post> findByUser(UserProfile userProfile);

    List<Post> findByUserIn(List<UserProfile> activeUserProfiles);

    @Query("SELECT p FROM Post p WHERE p.user = :user AND p.createdAt >= :startDate AND p.createdAt <= :endDate")
    List<Post> findPostsByUserAndDateRange(@Param("user") UserProfile user, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    List<Post> findByUserAndType(UserProfile user, PostType postType);

    List<Post> findPostByCategory(String topic);

    List<Post> findByDocTitleContainingIgnoreCase(String query);

    List<Post> findByDescriptionContainingIgnoreCase(String query);

    @Query("SELECT p FROM Post p WHERE UPPER(p.category) LIKE UPPER(CONCAT('%', :query, '%'))")
    List<Post> findByCategoryContainingIgnoreCase(@Param("query") String query);

    List<Post> findByOtherCategoryContainingIgnoreCase(String query);
}
