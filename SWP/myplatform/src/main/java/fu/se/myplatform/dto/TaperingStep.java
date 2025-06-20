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
<<<<<<< HEAD
    public String note;

=======
>>>>>>> 4a1f5c0c39b43b62e434892413b2abeb7b8f2c9c
}
