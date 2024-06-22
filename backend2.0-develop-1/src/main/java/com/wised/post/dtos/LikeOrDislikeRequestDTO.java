package com.wised.post.dtos;


import com.wised.post.enums.LikeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LikeOrDislikeRequestDTO {
        private Integer postId;
        private Integer userId;
        private LikeType likeType;
}
