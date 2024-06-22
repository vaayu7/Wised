package com.wised.post.model;

import lombok.*;

import jakarta.persistence.*;
import net.minidev.json.annotate.JsonIgnore;

import java.util.Date;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Mention")
public class Mention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "created_at")
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }

    @ManyToOne
    @JoinColumn(name = "post_id")
    @ToString.Exclude
    @JsonIgnore
    private Post post;

    private Integer mentioned;
}

