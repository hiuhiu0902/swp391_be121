// ProfileResponse.java
package fu.se.myplatform.dto;

import lombok.Data;

@Data
public class ProfileResponse {
    private String userName;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String role;
}