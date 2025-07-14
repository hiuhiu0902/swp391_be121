package fu.se.myplatform.api;

import fu.se.myplatform.dto.ChatMessageDTO;
import fu.se.myplatform.entity.ChatMessage;
import fu.se.myplatform.entity.Member;
import fu.se.myplatform.service.ChatMessageService;
import fu.se.myplatform.service.CoachService;
import fu.se.myplatform.service.MemberService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@SecurityRequirement(
        name = "api"
)
public class ChatAPI {
    @Autowired
    MemberService memberService;

    @Autowired
    CoachService coachService;

    @Autowired
    ChatMessageService chatMessageService;
    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @GetMapping("/history")
    public List<ChatMessageDTO> getHistory(@RequestParam Long memberId, @RequestParam Long coachId) {
        return chatMessageService.getChatHistory(memberId, coachId);
    }
    @PostMapping("/send")
    public ChatMessageDTO send(@RequestBody ChatMessageDTO chatMessageDTO) {
        // Test: bạn tự điền userId và senderIsCoach đúng logic (hoặc truyền qua body)
        Long userId = 6L; // Tạm hardcode
        boolean senderIsCoach = false;
        return chatMessageService.sendMessage(chatMessageDTO, userId);
    }
  }
