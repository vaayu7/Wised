package com.wised.auth.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.wised.auth.enums.EducationType;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "education")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private EducationType type;

    @Column(name = "type_id")
    private Integer typeId;

    @Column(name = "specialization_stream")
    private String specializationStream;

    @Column(name = "from_date")
    private Date fromDate;

    @Column(name = "to_date")
    private Date toDate;

    @Column(name = "is_currently_studying")
    private boolean currentlyStudying;

    @Column(name = "current_year_class")
    private Integer currentYearClass;

    @Column(name = "current_semester")
    private Integer currentSemester;

//    @ToString.Exclude
//    @ManyToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "college_id")
//    private College college;
//
//    @ToString.Exclude
//    @ManyToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "school_id")
//    private School school;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile user;
}
