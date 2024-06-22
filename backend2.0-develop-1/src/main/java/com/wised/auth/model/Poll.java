package com.wised.auth.model;

import com.wised.auth.enums.PollType;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Poll")
public class Poll {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;


    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserProfile user;  // You should create a User entity for the user relationship



    @Enumerated(EnumType.STRING)
    private PollType type;



    @Column(name = "created_at")
    private Date createdAt;



    @Column(name = "is_active")
    private boolean isActive;



    private String question;


    @ToString.Exclude
    @ElementCollection
    @CollectionTable(name = "poll_options", joinColumns = @JoinColumn(name = "poll_id"))
    @Column(name = "option")
    private List<String> options;


    @ToString.Exclude
    @ElementCollection
    @CollectionTable(name = "poll_answer", joinColumns = @JoinColumn(name = "poll_id"))
    @Column(name = "answer")
    private List<String> answer;


    @ToString.Exclude
    @ElementCollection
    @CollectionTable(name = "poll_response", joinColumns = @JoinColumn(name = "poll_id"))
    @Column(name = "response")
    private List<String> pollResponse;



    @Column(name = "anonymous")
    private boolean anonymous;


    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }

}



