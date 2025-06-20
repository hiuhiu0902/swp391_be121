package fu.se.myplatform.dto;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaperingStep {
    public int weekNumber;
    public LocalDate startDate;
    public LocalDate endDate;
    public int cigarettesPerDay;
    public String note;

}
