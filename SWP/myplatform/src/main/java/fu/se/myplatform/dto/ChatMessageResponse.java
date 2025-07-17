package fu.se.myplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageResponse {
    public Long id;
    public String content;
    public LocalDateTime sentAt;
    public boolean senderIsCoach; // true: coach gửi, false: member gửi
    public Long memberId;
    public Long coachId;
    private String senderName;
}
