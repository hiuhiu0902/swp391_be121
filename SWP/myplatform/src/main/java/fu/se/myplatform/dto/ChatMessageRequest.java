package fu.se.myplatform.dto;
import lombok.Data;

@Data
public class ChatMessageRequest {
    private String content;
    private boolean senderIsCoach;
    private Long memberId;
    private Long coachId;
}