package com.wised.auth.model;
import com.wised.auth.enums.InterestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_input_addition")
public class UserInputAddition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private InterestType type;

    @Column(name = "addition")
    private String addition;

    @Column(name = "count")
    private Integer count;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserProfile userProfile;
}
