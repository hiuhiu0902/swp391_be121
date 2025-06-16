package fu.se.myplatform.dto;

import lombok.Data;

@Data
public class ProfileRequest {
    private String fullName;
    private String email;
    private String phone;
    private String address;
}