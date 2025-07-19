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
            System.out.println("Sending message to topic: " + topic);
            System.out.println("Message content: " + response.getContent());

            // Gửi tin nhắn mới tới topic WebSocket
            messagingTemplate.convertAndSend(topic, response);

        } catch (Exception e) {
            // Xử lý lỗi và gửi thông báo lỗi qua WebSocket
            e.printStackTrace();
            messagingTemplate.convertAndSend("/topic/chat.error", "Error occurred while sending message.");
        }
    }

    @MessageMapping("/chat.history")
    public void sendChatHistory(@Payload ChatMessageRequest chatRequest) {
        try {
            List<ChatMessageResponse> chatHistory = chatMessageService.getChatHistory(chatRequest.getMemberId(), chatRequest.getCoachId());

            if (chatHistory.isEmpty()) {
                System.out.println("No chat history available for member: " + chatRequest.getMemberId());
                messagingTemplate.convertAndSend("/topic/chat." + chatRequest.getMemberId() + "." + chatRequest.getCoachId(), "No chat history available.");
                return;
            }

            String topic = "/topic/chat." + chatRequest.getMemberId() + "." + chatRequest.getCoachId();

            // Gửi lịch sử chat chỉ khi người dùng yêu cầu
            chatHistory.forEach(response -> {
                messagingTemplate.convertAndSend(topic, response);
            });

        } catch (Exception e) {
            e.printStackTrace();
            messagingTemplate.convertAndSend("/topic/chat." + chatRequest.getMemberId() + "." + chatRequest.getCoachId(), "Error occurred while retrieving chat history.");
        }
    }


}


