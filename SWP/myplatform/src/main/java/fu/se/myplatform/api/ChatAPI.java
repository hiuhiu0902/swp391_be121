package fu.se.myplatform.api;

import fu.se.myplatform.dto.ChatMessageDTO;
import fu.se.myplatform.entity.ChatMessage;
import fu.se.myplatform.entity.Member;
import fu.se.myplatform.service.ChatMessageService;
import fu.se.myplatform.service.CoachService;
import fu.se.myplatform.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatAPI {
    @Autowired
    MemberService memberService;

    @Autowired
    CoachService coachService;

    @Autowired
    ChatMessageService chatMessageService;
    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/send")
    public void sendMessage(@Payload ChatMessageDTO chatDTO) {
        // Check quyền trước khi cho phép gửi tin
        boolean canChat = false;

        // Xác định vai trò của sender (dùng SecurityContextHolder để lấy role nếu đã cấu hình security)
        // Giả sử bạn truyền senderRole từ client hoặc lấy từ token
        String senderRole = chatDTO.getSenderRole();

        if ("MEMBER".equalsIgnoreCase(senderRole)) {
            // Kiểm tra member này đã assign đúng coach chưa
            Member member = memberService.getMemberProfile(chatDTO.getSenderId());
            if (member.getCoach() != null && member.getCoach().getCoachId().equals(chatDTO.getReceiverId())) {
                canChat = true;
            }
        } else if ("COACH".equalsIgnoreCase(senderRole)) {
            // Kiểm tra coach này có phải là coach của member này không
            Member member = memberService.getMemberProfile(chatDTO.getReceiverId());
            if (member.getCoach() != null && member.getCoach().getCoachId().equals(chatDTO.getSenderId())) {
                canChat = true;
            }
        }

        if (!canChat) {
            // Nếu không hợp lệ thì không gửi, có thể gửi thông báo lỗi về cho client
            return;
        }

        // Lưu và gửi tin nhắn như cũ
        ChatMessage chat = new ChatMessage();
        chat.setSenderId(chatDTO.getSenderId());
        chat.setReceiverId(chatDTO.getReceiverId());
        chat.setContent(chatDTO.getContent());
        chat.setTimestamp(LocalDateTime.now());
        chatMessageService.save(chat);

        messagingTemplate.convertAndSendToUser(
                chatDTO.getReceiverId().toString(),
                "/queue/messages",
                chatDTO
        );
    }
}
