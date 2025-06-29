package fu.se.myplatform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "smoking_records")
@Getter
@Setter
public class SmokingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Account account;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "cigarettes_smoked", nullable = false)
    private int cigarettesSmoked;
}
