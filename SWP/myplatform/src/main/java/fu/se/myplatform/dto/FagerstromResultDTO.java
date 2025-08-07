package fu.se.myplatform.dto;

import fu.se.myplatform.enums.DependencyLevel;
import lombok.Data;

@Data
public class FagerstromResultDTO {
    public int score;
    public DependencyLevel level;
    public long durationWeek;

    public FagerstromResultDTO(int score, DependencyLevel level, long durationWeek) {
        this.score = score;
        this.level = level;
        this.durationWeek = durationWeek;
    }
}
