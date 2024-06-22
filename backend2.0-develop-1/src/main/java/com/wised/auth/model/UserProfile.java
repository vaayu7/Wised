package com.wised.auth.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.wised.auth.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_profile", uniqueConstraints = @UniqueConstraint(columnNames = {"userName"}))
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private Date dob;
    private Gender gender;
    private String contactNumber;

    @Transient
    private String email; // Transient field to store the associated User's email

    @Column(unique = true)
    private String userName;
    private String fullName;

    private String city;
    private String country;
    private  List<String> preferredLanguage;
    private List<String> genre;
    private  List<String> socialMediaLinks;
    private String profileImageUrl;
    private String coverImageUrl;
    private boolean isActive;
    private String bio;
    private Date createdAt;
    private Date updatedAt;
    private String deviceOs;
    private Date lastLogin;

    private String privacySettings;
    private String notificationSettings;
    private String userRole;
    private boolean verificationStatus;
    private String ipAddress;
    private Date lastPasswordChange;

    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "user_id") // Name of the foreign key column in the user_profile table
    private User user;

    @ToString.Exclude
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Content> contents = new ArrayList<>();

    public UserProfile(Integer id, String fullName, String email) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProfile)) return false;
        UserProfile that = (UserProfile) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user);
    }


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
