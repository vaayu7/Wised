package com.wised.post.repository;

import com.wised.post.model.Mention;
import com.wised.post.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MentionRepository extends JpaRepository<Mention, Long> {
    Optional<Mention> findByPost(Post post);
    // Add custom query methods if needed
}
