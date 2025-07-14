package fu.se.myplatform.controller;

import fu.se.myplatform.dto.ChatMessageDTO;
import fu.se.myplatform.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class ChatWebSocketController {
    @Autowired
    ChatMessageService chatMessageService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO chatMessageDTO, @Headers Map<String, Object> headers) {
        // Giả sử userId được gửi kèm từ FE (sau này nên lấy từ token/session)
        Long userId = Long.parseLong(headers.get("userId").toString());
        chatMessageService.sendMessage(chatMessageDTO, userId);
    }
}
