package com.wised.helpandsettings.model;

import com.wised.auth.model.UserProfile;
import com.wised.helpandsettings.enums.HelpStatus;
import lombok.*;
import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "help_request")
public class Help {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String email;

    private String subject;

    @ElementCollection
    private String[] issue;

    private String description;

    @ElementCollection
    private List<String> awsUrl;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "resolved_at")
    private Date resolvedAt;

    @Enumerated(EnumType.STRING)
    private HelpStatus helpStatus;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile userProfile;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        resolvedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        resolvedAt = new Date();
    }
}
