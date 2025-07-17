package fu.se.myplatform.controller;

import fu.se.myplatform.dto.ChatMessageRequest;
import fu.se.myplatform.dto.ChatMessageResponse;
import fu.se.myplatform.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ChatWebSocketController {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Gửi tin nhắn qua WebSocket
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageRequest chatRequest) {
        try {
            // Kiểm tra xem member và coach đã được assign chưa
            boolean isAssigned = chatMessageService.isAssigned(chatRequest.getMemberId(), chatRequest.getCoachId());
            if (!isAssigned) {
                messagingTemplate.convertAndSend("/topic/chat.error", "You are not assigned to this coach.");
                return; // Dừng lại nếu chưa assign
            }

            // Lưu tin nhắn và tạo phản hồi
            ChatMessageResponse response = chatMessageService.saveMessage(chatRequest);

            // Tạo topic theo memberId và coachId
            String topic = "/topic/chat." + response.getMemberId() + "." + response.getCoachId();

            // Gửi tin nhắn tới topic WebSocket
            messagingTemplate.convertAndSend(topic, response);

        } catch (Exception e) {
            // Xử lý lỗi và gửi thông báo lỗi qua WebSocket
            e.printStackTrace();
            messagingTemplate.convertAndSend("/topic/chat.error", "Error occurred while sending message.");
        }
    }

    // Lịch sử chat (Lấy lịch sử giữa member và coach)
    @MessageMapping("/chat.history")
    public void sendChatHistory(@Payload ChatMessageRequest chatRequest) {
        try {
            // Lấy lịch sử chat từ service
            List<ChatMessageResponse> chatHistory = chatMessageService.getChatHistory(chatRequest.getMemberId(), chatRequest.getCoachId());

            // Kiểm tra nếu không có lịch sử chat
            if (chatHistory.isEmpty()) {
                messagingTemplate.convertAndSend("/topic/chat." + chatRequest.getMemberId() + "." + chatRequest.getCoachId(), "No chat history available.");
                return;
            }

            // Gửi lại lịch sử chat qua WebSocket
            String topic = "/topic/chat." + chatRequest.getMemberId() + "." + chatRequest.getCoachId();
            chatHistory.forEach(response -> messagingTemplate.convertAndSend(topic, response));

        } catch (Exception e) {
            // Xử lý lỗi và gửi thông báo lỗi qua WebSocket
            e.printStackTrace();
            messagingTemplate.convertAndSend("/topic/chat." + chatRequest.getMemberId() + "." + chatRequest.getCoachId(), "Error occurred while retrieving chat history.");
        }
    }
}


