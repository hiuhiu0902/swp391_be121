package fu.se.myplatform.dto;

import fu.se.myplatform.enums.QuitReason;
import fu.se.myplatform.enums.SupportMethod;
import fu.se.myplatform.enums.Triggers;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
public class QuitPlanRequest {
    private LocalDate startDate;
    private int numberOfCigarettes;
    private BigDecimal pricePerPack;
    private Set<QuitReason> reasons;
    private Set<Triggers> triggers;
    private Set<SupportMethod> supportMethods;
    private int durationWeeks;
}
