package com.wised.bystream.dtos;

import com.wised.post.model.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ContentDto {
    private String messege;
    private String error;
    private List<Post> posts;

}
