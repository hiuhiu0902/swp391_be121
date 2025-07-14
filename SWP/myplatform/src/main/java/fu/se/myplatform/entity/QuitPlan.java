package fu.se.myplatform.entity;

import fu.se.myplatform.dto.TaperingStep;
import fu.se.myplatform.enums.QuitReason;
import fu.se.myplatform.enums.SupportMethod;
import fu.se.myplatform.enums.Triggers;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "quit_plans")
@Getter
@Setter
public class QuitPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startDate;
    private int cigarettesPerDay;
    private BigDecimal pricePerPack;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "plan_reasons",
        joinColumns = @JoinColumn(name = "plan_id")
    )
    @Enumerated(EnumType.STRING)
    private Set<QuitReason> reasons = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "plan_triggers",
        joinColumns = @JoinColumn(name = "plan_id")
    )
    @Enumerated(EnumType.STRING)
    private Set<Triggers> triggers = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "plan_supportmethods",
        joinColumns = @JoinColumn(name = "plan_id")
    )
    @Enumerated(EnumType.STRING)
    private Set<SupportMethod> supportMethods = new HashSet<>();

    private BigDecimal dailyCost;
    private BigDecimal weeklyCost;
    private BigDecimal monthlyCost;
    private BigDecimal yearlyCost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Account account;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "plan_tapering_steps",
        joinColumns = @JoinColumn(name = "plan_id")
    )
    private List<TaperingStep> taperingSchedule = new ArrayList<>();

    @OneToMany(
        mappedBy = "quitPlan",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<QuitProgress> progressList = new ArrayList<>();

    public void clearCollections() {
        if (this.reasons != null) this.reasons.clear();
        if (this.triggers != null) this.triggers.clear();
        if (this.supportMethods != null) this.supportMethods.clear();
        if (this.taperingSchedule != null) this.taperingSchedule.clear();
        if (this.progressList != null) {
            this.progressList.forEach(progress -> progress.setQuitPlan(null));
            this.progressList.clear();
        }
    }
}
