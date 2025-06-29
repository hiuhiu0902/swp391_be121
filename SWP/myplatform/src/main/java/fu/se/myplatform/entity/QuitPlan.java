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
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "quit_plans")
@Getter
@Setter
public class QuitPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    public LocalDate startDate;
    public int cigarettesPerDay;
    public BigDecimal pricePerPack;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "plan_reasons"
            ,joinColumns = @JoinColumn(
            name = "plan_id"))
    @Enumerated(EnumType.STRING)
    public Set<QuitReason> reasons;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "plan_triggers"
            ,joinColumns = @JoinColumn(
            name = "plan_id"))
    @Enumerated(EnumType.STRING)
    public Set<Triggers> triggers;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "plan_supportmethods"
            ,joinColumns = @JoinColumn(
            name = "plan_id"))
    @Enumerated(EnumType.STRING)
    public Set<SupportMethod> supportMethods;

    public BigDecimal dailyCost;
    public BigDecimal weeklyCost;
    public BigDecimal monthlyCost;
    public BigDecimal yearlyCost;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public Account account;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "plan_tapering_steps", joinColumns = @JoinColumn(name = "plan_id"))
    private List<TaperingStep> taperingSchedule;

    @OneToMany(mappedBy = "quitPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuitProgress> progressList;

}
