package com.wised.auth.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Data
@Entity
@Table(name = "UsertoPollMapper")
public class UsertoPollMapper {

    @Getter
    @Setter

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @ManyToOne
    @JoinColumn(name = "poll_id", referencedColumnName = "id")
    private Poll poll;


    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;


    @ElementCollection
    @CollectionTable(name = "user_response", joinColumns = @JoinColumn(name = "mapper_id"))
    @Column(name = "response")
    private List<String> userResponse;

}