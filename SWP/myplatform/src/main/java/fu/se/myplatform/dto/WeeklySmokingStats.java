package fu.se.myplatform.dto;

import lombok.Data;

@Data
public class WeeklySmokingStats {
    public int weekNumber;
    public int planTargetCigarettes;
    public int cigarettesSmoked;
}
