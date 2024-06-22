package com.wised.people.model;

import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="block")
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "blocker")
    @ToString.Exclude
    private UserProfile blocker;

    @ManyToOne
    @JoinColumn(name = "blocked")
    @ToString.Exclude
    private UserProfile blocked;


    @Column(name = "created_at")
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }

}