package fu.se.myplatform.controller;

import fu.se.myplatform.dto.ChatMessageRequest;
import fu.se.myplatform.dto.ChatMessageResponse;
import fu.se.myplatform.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {
    @Autowired
    ChatMessageService chatMessageService;
    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageRequest chatRequest) {
        try {
            ChatMessageResponse response = chatMessageService.saveMessage(chatRequest);
            String topic = "/topic/chat." + response.getMemberId() + "." + response.getCoachId();
            messagingTemplate.convertAndSend(topic, response);
        } catch (Exception e) {
            // Gửi lỗi về topic hoặc log lỗi
        }
    }
}
