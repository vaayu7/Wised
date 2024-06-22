package com.wised.post.dtos;

import com.wised.post.enums.PostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDTO {
    private int views;

    private String description;

    private String docTitle;

    private List<String> awsUrl;

    private Integer user;

    private int likes;

    private int dislikes;

    private String shareUrl;

    private List<Integer> userMentionedIds;

    private PostType postType;

}
