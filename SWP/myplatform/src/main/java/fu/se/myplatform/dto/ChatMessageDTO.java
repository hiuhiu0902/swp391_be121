package fu.se.myplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageDTO {
    public Long id;
    public String content;
    public LocalDateTime sentAt;
    public boolean senderIsCoach; // true: coach gửi, false: member gửi
    public Long memberId;
    public Long coachId;
    // Thêm tên người gửi nếu muốn hiển thị trên FE
    public String senderName;
}
