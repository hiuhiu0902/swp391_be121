// src/main/java/fu/se/myplatform/entity/Assessment.java
package fu.se.myplatform.entity;

import fu.se.myplatform.enums.DependencyLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "assessments")
@Getter
@Setter
public class Assessment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int score;

    @Enumerated(EnumType.STRING)
    private DependencyLevel dependencyLevel;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Account account;
}