package fu.se.myplatform.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserRankingDTO {
    private Long accountId;
    private String username;
    private String avatarUrl;
    private BigDecimal totalMoneySaved;
    private int participationScore;
    private int rank;
}
