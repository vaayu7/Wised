package com.wised.auth.model;

import com.wised.auth.enums.ContentType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "content")
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "aws_url")
    private String awsUrl;

    @Column(name = "document_key")
    private  String key;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ContentType type;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "title")
    private String title;

    @Column(name = "likes")
    private Integer likes;

    @Column(name = "dislikes")
    private Integer dislikes;

    @Column(name = "share_url")
    private String shareUrl;

    @Column(name = "category")
    private String category;

    @Column(name = "ratings")
    private BigDecimal ratings;

    @Column(name = "skill_set", columnDefinition = "text")
    private String skillSet;

    @Column(name = "certificate_template", columnDefinition = "text")
    private String certificateTemplate;

    @Column(name = "thumbnail")
    private String thumbnail;

    @Column(name = "feedback", columnDefinition = "text")
    private String feedback;

    @Column(name = "views")
    private Integer views;

    @Column(name = "share_count")
    private Integer shareCount;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserProfile user;

    // Constructors, getters, setters, and other methods
}

