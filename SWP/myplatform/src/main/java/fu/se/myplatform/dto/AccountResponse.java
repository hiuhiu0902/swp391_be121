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

    public java.time.LocalDateTime createdAt;

    // Thông tin riêng cho từng loại tài khoản, gom chung vào DTO này
    public Boolean isVip;
    public java.time.LocalDate vipStartDate;
    public java.time.LocalDate vipExpiryDate;
    public String memberStatus;
    public String staffPosition;
    public String staffStatus;
    public String coachAddress;
    public String coachStatus;

}
