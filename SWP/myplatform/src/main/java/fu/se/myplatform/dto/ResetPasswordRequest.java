package fu.se.myplatform.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    @Size(min = 6, message = "Password must be at least 6 characters long")
    public String password;
}
