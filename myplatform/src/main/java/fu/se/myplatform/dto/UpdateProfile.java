package fu.se.myplatform.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfile {
    private String email;
    private String phoneNumber;
    private String gender;

}
