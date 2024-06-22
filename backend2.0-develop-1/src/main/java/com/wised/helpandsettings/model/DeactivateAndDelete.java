package com.wised.helpandsettings.model;

import com.wised.auth.model.UserProfile;
import lombok.*;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "deactivate_delete")
public class DeactivateAndDelete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "is_deactivated")
    private boolean isDeactivated;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "reason_id", referencedColumnName = "id")
    private Reason reason;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Column(name = "deletion_pending")
    private boolean deletionPending;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserProfile user;
}
