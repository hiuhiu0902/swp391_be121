package fu.se.myplatform.dto;

import fu.se.myplatform.enums.Gender;
import fu.se.myplatform.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAccountRequest {
    private String fullName;
    private String email;
    private String phoneNumber;
    private Gender gender;
    private Role role;
    private String status;
    private String password; // optional, for reset password
    // Staff
    private String position;
    // Coach
    private String address;
    // Member
    private Boolean isVip;
    private String vipStartDate;
    private String vipExpiryDate;
}
