package com.wised.people.model;

import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="follow")
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "follower")
    @ToString.Exclude
    private UserProfile follower;

    @ManyToOne
    @JoinColumn(name = "followee")
    @ToString.Exclude
    private UserProfile followee;

    @Column(name = "created_at")
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }
}
