package fu.se.myplatform.api;

import fu.se.myplatform.dto.ChatMessageRequest;
import fu.se.myplatform.dto.ChatMessageResponse;
import fu.se.myplatform.dto.UserBasicInfoResponse;
import fu.se.myplatform.service.ChatMessageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@SecurityRequirement(
        name = "api"
)
public class ChatAPI {

    @Autowired
    ChatMessageService chatMessageService;

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody ChatMessageRequest chatRequest) {
        try {
            ChatMessageResponse response = chatMessageService.saveMessage(chatRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageResponse>> getHistory(
            @RequestParam Long memberId, @RequestParam Long coachId
    ) {
        List<ChatMessageResponse> messages = chatMessageService.getChatHistory(memberId, coachId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/assignable-coaches")
    public ResponseEntity<List<UserBasicInfoResponse>> getAssignableCoaches(@RequestParam Long memberId) {
        List<UserBasicInfoResponse> list = chatMessageService.getAssignableCoaches(memberId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/assignable-members")
    public ResponseEntity<List<UserBasicInfoResponse>> getAssignableMembers(@RequestParam Long coachId) {
        List<UserBasicInfoResponse> list = chatMessageService.getAssignableMembers(coachId);
        return ResponseEntity.ok(list);
    }
}
