package fu.se.myplatform.dto;

import fu.se.myplatform.enums.QuitReason;
import fu.se.myplatform.enums.SupportMethod;
import fu.se.myplatform.enums.Triggers;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
public class QuitPlanResponse {
    private long id;
    private LocalDate startDate;
    private int numberOfCigarettes;
    private BigDecimal pricePerPack;
    private Set<QuitReason> reasons;
    private Set<Triggers> triggers;
    private Set<SupportMethod> supportMethods;
    private BigDecimal dailyCost;
    private BigDecimal weeklyCost;
    private BigDecimal monthlyCost;
    private BigDecimal yearlyCost;
    private int durationWeeks;
    private List<TaperingStep> taperingSchedule;
    private List<String> tips;
}
