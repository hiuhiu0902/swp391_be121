package fu.se.myplatform.service;

import fu.se.myplatform.dto.AccountResponse;
import fu.se.myplatform.dto.ChatMessageResponse;
import fu.se.myplatform.dto.LoginRequest;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.repository.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@SpringBootTest
public class ChatMessageServiceTest {

	@Autowired
	private ChatMessageService chatMessageService;

	@Autowired
	private ChatMessageRepository chatMessageRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private CoachRepository coachRepository;

	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private AuthenticationRepository authenticationRepository;
	@Autowired
	private AuthenticationService authenticationService;

	// Phương thức giúp đăng nhập user vào hệ thống trong quá trình test
	private void login(String username, String password) {
		// Tạo đối tượng tài khoản từ cơ sở dữ liệu (giả sử dữ liệu đã có trong DB)
		Account account = accountRepository.findByUserName(username);
		// Kiểm tra mật khẩu và tạo đối tượng authentication
		if (account.getPassword().equals(password)) {
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, password);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} else {
			throw new RuntimeException("Invalid credentials");
		}
	}

	@Test
	public void getChatHistory() {
		// Đăng nhập với tài khoản string04
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setUserName("string04");
		loginRequest.setPassword("string");
		AccountResponse response = authenticationService.login(loginRequest);

		// Lấy ID của Member và Coach từ cơ sở dữ liệu (giả sử là memberId = 10 và coachId = 1)
		Long memberId = response.memberId;  // ID của member cần lấy lịch sử chat
		Long coachId = response.coachId;    // ID của coach cần lấy lịch sử chat

		// Lấy lịch sử chat từ service
		List<ChatMessageResponse> chatHistory = chatMessageService.getChatHistory(memberId, coachId);

		// In thử ra lịch sử chat
		System.out.println("Chat history for memberId: " + memberId + " and coachId: " + coachId);
		for (ChatMessageResponse msg : chatHistory) {
			System.out.println("Content: " + msg.getContent() + ", SentAt: " + msg.getSentAt() + ", Sender: " + msg.getSenderName());
		}

		// Kiểm tra xem có dữ liệu trả về không
		assertNotNull(chatHistory, "Chat history should not be null");
		assertTrue(chatHistory.size() > 0, "Chat history should contain messages");
	}
}
