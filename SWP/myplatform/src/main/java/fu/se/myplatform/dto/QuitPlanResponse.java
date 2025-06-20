package fu.se.myplatform.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class QuitPlanResponse {
    public LocalDate startDate; // ngày bắt đầu cai
    public BigDecimal dailyCost;
    public BigDecimal weeklyCost;
    public BigDecimal monthlyCost;
    public BigDecimal yearlyCost;

    public List<TaperingStep> taperingSchedule;
    private List<String> tips;

    private int durationWeeks;
    private int numberOfCigarettes;
    private BigDecimal pricePerPack;
    private List<String> reasons;
    private List<String> triggers;
}
