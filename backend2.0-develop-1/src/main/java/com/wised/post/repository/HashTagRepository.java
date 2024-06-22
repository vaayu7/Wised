package com.wised.post.repository;


import com.wised.post.model.HashTag;
import com.wised.post.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HashTagRepository extends JpaRepository<HashTag, Long> {

    Optional<HashTag> findByHashtag(String hashtag);

    List<Post> findByHashtagContainingIgnoreCase(String query);
}

