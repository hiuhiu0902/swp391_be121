package fu.se.myplatform.dto;

import fu.se.myplatform.enums.Role;
import lombok.Data;

@Data
public class UpdateRoleRequest {
    public long userId;
    public Role role;
}
