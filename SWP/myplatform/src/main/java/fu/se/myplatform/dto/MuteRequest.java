package fu.se.myplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MuteRequest {
    @NotBlank(message = "Reason must be provided")
    private String reason;
    private LocalDateTime mutedUntil;  // null means mute permanently
}
