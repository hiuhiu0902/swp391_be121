package fu.se.myplatform.dto;

import lombok.Data;

@Data
public class RatingResponse {
    private Long ratingId;
    private Long coachId;
    private String coachName;
    private int stars;
    private String comment;
    private String memberName; // optional
}
