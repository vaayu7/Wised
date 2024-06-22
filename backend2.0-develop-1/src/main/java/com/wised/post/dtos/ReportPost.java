package com.wised.post.dtos;

import com.wised.auth.model.UserProfile;
import com.wised.people.enums.ReportReason;
import com.wised.post.model.Post;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="reportpost")
public class ReportPost {
    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer Id;

    @ManyToOne
    @JoinColumn(name = "reporter_id")  // assuming "reporter_id" is the foreign key column in the ReportPost table
    @ToString.Exclude
    private UserProfile reporter;

    @ManyToOne
    @JoinColumn(name = "post_id")  // assuming "post_id" is the foreign key column in the ReportPost table
    @ToString.Exclude
    private Post post;


    @Column(nullable = false)
    @Enumerated
    private ReportReason reportReason;

    @Column(name = "other_reason")
    private String otherReason;

    @Column(name = "created_at")
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }
}
