package fu.se.myplatform.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViewProfile {


    private String userName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String gender;
    private String profilePicture;
    private String role;
    private boolean enabled;


    // Getters and Setters
}