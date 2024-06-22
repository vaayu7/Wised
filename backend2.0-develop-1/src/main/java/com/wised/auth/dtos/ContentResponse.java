package com.wised.auth.dtos;

import com.wised.auth.enums.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContentResponse {
    private Integer id;
    private String awsUrl;
    private String key;
    private ContentType type;
    private String description;
    private String title;
    private Integer likes;
    private Integer dislikes;
    private String shareUrl;
    private String category;
    private BigDecimal ratings;
    private String skillSet;
    private String certificateTemplate;
    private String thumbnail;
    private String feedback;
    private Integer views;
    private Integer shareCount;
    private String userEmail;
}
