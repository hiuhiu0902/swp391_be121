package fu.se.myplatform.dto;

import lombok.Data;

@Data
public class UserBasicInfoResponse {
    private Long id;          // memberId hoặc coachId
    private String fullName;
    private String avatarUrl; // Nếu có
}