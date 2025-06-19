package fu.se.myplatform.dto;

import fu.se.myplatform.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAccountRequest {
    private String userName;
    private String password;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Role role;
    // Thông tin riêng cho từng role
    private String status; // Member, Staff, Coach
    // Coach
}

