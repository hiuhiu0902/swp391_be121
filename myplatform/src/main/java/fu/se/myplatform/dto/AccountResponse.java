package fu.se.myplatform.dto;

import fu.se.myplatform.enums.Gender;
import fu.se.myplatform.enums.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class AccountResponse {

    public String userName;

    public String password;

    public String email;

    public String phoneNumber;

    public Role role;

    public Gender gender;

    public String token;

}
