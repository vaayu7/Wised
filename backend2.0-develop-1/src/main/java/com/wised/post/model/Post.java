package com.wised.post.model;


import com.wised.auth.model.UserProfile;
import com.wised.post.enums.Category;
import jakarta.persistence.*;
import lombok.*;
import  com.wised.post.enums.PostType;
import net.minidev.json.annotate.JsonIgnore;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "doc_title")
    private String docTitle;

    @ElementCollection
    @CollectionTable(name = "post_aws_urls", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "aws_url")
    private List<String> awsUrl;

    private String description;
    private int views;

    @Enumerated(EnumType.STRING)
    private PostType type;

    @Enumerated(EnumType.STRING)
    private Category category;

    private String otherCategory;

    @Column(name= "share_count")
    private Integer shareCount;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    private String shareUrl;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @JsonIgnore
    private UserProfile user;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
