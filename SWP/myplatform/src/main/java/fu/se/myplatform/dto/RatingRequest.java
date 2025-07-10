package fu.se.myplatform.dto;

import lombok.Data;

@Data
public class RatingRequest {
    private int stars;
    private String comment;
    // getters/setters
}
