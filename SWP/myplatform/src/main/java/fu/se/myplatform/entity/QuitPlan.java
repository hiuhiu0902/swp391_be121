package fu.se.myplatform.entity;

import fu.se.myplatform.dto.TaperingStep;
import fu.se.myplatform.enums.QuitReason;
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
<<<<<<< HEAD
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

    public BigDecimal dailyCost;
    public BigDecimal weeklyCost;
    public BigDecimal monthlyCost;
    public BigDecimal yearlyCost;

=======

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

    public BigDecimal dailyCost;
    public BigDecimal weeklyCost;
    public BigDecimal monthlyCost;
    public BigDecimal yearlyCost;

>>>>>>> 4a1f5c0c39b43b62e434892413b2abeb7b8f2c9c
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public Account account;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "plan_tapering_steps", joinColumns = @JoinColumn(name = "plan_id"))
    private List<TaperingStep> taperingSchedule;

}
