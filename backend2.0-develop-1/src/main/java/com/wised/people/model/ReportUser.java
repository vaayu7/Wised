package com.wised.people.model;

import com.wised.auth.model.User;
import com.wised.auth.model.UserProfile;
import com.wised.people.enums.ReportReason;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="reportuser")
public class ReportUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer Id;

    @ManyToOne
    @JoinColumn(name = "reporter_id")  // assuming "reporter_id" is the foreign key column in the ReportUser table
    @ToString.Exclude
    private UserProfile reporter;

    @ManyToOne
    @JoinColumn(name = "reported_user_id")  // assuming "reported_user_id" is the foreign key column in the ReportUser table
    @ToString.Exclude
    private UserProfile reported;


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

