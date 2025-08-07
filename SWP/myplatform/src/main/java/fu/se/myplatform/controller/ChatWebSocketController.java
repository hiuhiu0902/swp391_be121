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
            String topic = "/topic/chat." + chatRequest.getMemberId() + "." + chatRequest.getCoachId();

            // Gửi toàn bộ danh sách lịch sử chat trong một lần duy nhất
            // Phía frontend sẽ nhận được một mảng (array) các đối tượng tin nhắn
            messagingTemplate.convertAndSend(topic, chatHistory);

            System.out.println("Sent " + chatHistory.size() + " historical messages to topic: " + topic);

        } catch (Exception e) {
            e.printStackTrace();
            String errorTopic = "/topic/chat." + chatRequest.getMemberId() + "." + chatRequest.getCoachId();
            messagingTemplate.convertAndSend(errorTopic, "Error occurred while retrieving chat history.");
        }
    }


}


