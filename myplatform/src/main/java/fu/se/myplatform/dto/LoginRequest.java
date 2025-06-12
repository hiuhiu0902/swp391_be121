package fu.se.myplatform.dto;

import lombok.Data;

@Data
public class LoginRequest {
    String userName;
    String password;
}
