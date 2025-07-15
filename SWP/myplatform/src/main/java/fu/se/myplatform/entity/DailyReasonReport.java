package fu.se.myplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "daily_reason_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyReasonReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quit_plan_id")
    private QuitPlan quitPlan;
}
