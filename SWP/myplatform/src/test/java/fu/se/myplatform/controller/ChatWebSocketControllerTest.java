//package fu.se.myplatform.controller;
//
//import fu.se.myplatform.dto.ChatMessageRequest;
//import fu.se.myplatform.service.ChatMessageService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.messaging.simp.stomp.StompSession;
//import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
//import org.springframework.web.socket.client.WebSocketStompClient;
//import org.springframework.web.socket.sockjs.SockJsClient;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//@SpringBootTest
//public class ChatWebSocketControllerTest {
//
//	@Autowired
//	private ChatWebSocketController chatWebSocketController;
//
//	@Autowired
//	private ChatMessageService chatMessageService;
//
//	@Test
//	public void testSendMessage() throws Exception {
//		// Khởi tạo WebSocket client
//		WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient());
//		StompSession stompSession = stompClient.connect("ws://localhost:8080/ws", new StompSessionHandlerAdapter() {}).get();
//
//		// Tạo tin nhắn giả để gửi
//		ChatMessageRequest chatRequest = new ChatMessageRequest();
//		chatRequest.setContent("Hi Coach, I need help!");
//		chatRequest.setSenderIsCoach(false);
//		chatRequest.setMemberId(6L);
//		chatRequest.setCoachId(1L);
//
//		// Gửi tin nhắn qua WebSocket
//		chatWebSocketController.sendMessage(chatRequest);
//
//		// Kiểm tra kết quả (kiểm tra sự tồn tại của stompSession sau khi gửi tin nhắn)
//		assertNotNull(stompSession);
//
//		// Đăng ký vào topic để nhận tin nhắn trả về
//		stompSession.subscribe("/topic/chat.6.1", new StompSessionHandlerAdapter());
//
//		// Gửi tin nhắn test qua WebSocket
//		stompSession.send("/app/chat.sendMessage", "{}");
//
//		// Kiểm tra xem tin nhắn có được nhận từ topic không
//		// Lưu ý: Bạn có thể thêm các assertion hoặc kiểm tra thêm tại đây.
//	}
//}
