package fu.se.myplatform.entity;

import fu.se.myplatform.enums.BusyLevel;
import fu.se.myplatform.enums.Mood;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
public class CravingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Member member;            // Người dùng (ROLE = MEMBER)
    @Enumerated(EnumType.STRING)
    private BusyLevel busy;
    @Enumerated(EnumType.STRING)
    private Mood mood;
    private LocalDateTime timestamp;
}
