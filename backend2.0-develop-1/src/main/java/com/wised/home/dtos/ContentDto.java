package com.wised.home.dtos;

import com.wised.post.model.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class ContentDto {
    private String message;
    private String error;
    private List<Post> posts;
    private Map<String, List<Post>> mappedPosts;
}
