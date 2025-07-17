package fu.se.myplatform.dto;

import lombok.Data;

@Data
public class MemberShortDTO {
    private Long memberId;
    private String fullName;
    private String status;
}
