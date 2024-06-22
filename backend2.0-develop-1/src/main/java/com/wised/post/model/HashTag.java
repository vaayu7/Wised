package com.wised.post.model;

import com.wised.auth.model.UserProfile;
import jakarta.persistence.*;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;

@Data
@Builder
@NoArgsConstructor
@Entity
@AllArgsConstructor
@Table(name = "HashTag")
public class HashTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String hashtag;

    @Column(name = "clicks_count")
    private Integer clicksCount;

    @Column(name = "search_count")
    private Integer searchCount;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    @ToString.Exclude
    private UserProfile creator;

    @ManyToOne
    @JoinColumn(name = "post_id")
    @ToString.Exclude
    @JsonIgnore
    private Post post;

}
