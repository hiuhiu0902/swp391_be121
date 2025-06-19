package fu.se.myplatform.service;

import fu.se.myplatform.dto.AccountResponse;
import fu.se.myplatform.dto.LoginRequest;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.enums.Role;
import fu.se.myplatform.exception.exception.AuthenticationException;
import fu.se.myplatform.repository.AuthenticationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

	@InjectMocks
	private AuthenticationService authenticationService;

	@Mock
	private AuthenticationRepository authenticationRepository;

	@Mock
	private TokenService tokenService;

	@Mock
	private EmailService emailService;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private ModelMapper modelMapper;

	private List<Account> accounts;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		accounts = new ArrayList<>();
		// Giả lập hành vi repository.save() để lưu vào danh sách tạm
		when(authenticationRepository.save(any(Account.class))).thenAnswer(invocation -> {
			Account acc = invocation.getArgument(0);
			accounts.add(acc);
			return acc;
		});
	}

	@Test
	void register_shouldAddAccountToList() {
		// Arrange
		Account account = new Account();
		account.setEmail("test@example.com");
		account.setPassword("password");
		account.setRole(Role.MEMBER);

		Account saved = authenticationService.register(account);

		assertEquals(1, accounts.size());
		assertEquals("test@example.com", accounts.get(0).getEmail());
		assertEquals(saved, accounts.get(0));
	}
	@Test
	void login_shouldReturnAccountResponse_whenCredentialsAreValid() {
		// Arrange
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setUserName("john_doe");
		loginRequest.setPassword("password123");

		Account account = new Account();
		account.setUserName("john_doe");
		account.setPassword("encoded_password");

		AccountResponse mockResponse = new AccountResponse();
		mockResponse.setUserName("john_doe");

		// Giả lập hành vi xác thực và ánh xạ
		when(authenticationManager.authenticate(any())).thenReturn(null); // Spring sẽ ném exception nếu fail
		when(authenticationRepository.findAccountByUserName("john_doe")).thenReturn(account);
		when(modelMapper.map(account, AccountResponse.class)).thenReturn(mockResponse);

		// Act
		AccountResponse response = authenticationService.login(loginRequest);

		// Assert
		assertNotNull(response);
		assertEquals("john_doe", response.getUserName());
	}

	@Test
	void login_shouldThrowAuthenticationException_whenCredentialsInvalid() {
		// Arrange
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setUserName("wrong_user");
		loginRequest.setPassword("wrong_pass");

		// Giả lập lỗi xác thực
		doThrow(new RuntimeException("Auth failed"))
				.when(authenticationManager).authenticate(any());

		// Act + Assert
		AuthenticationException thrown = assertThrows(AuthenticationException.class, () -> {
			authenticationService.login(loginRequest);
		});

		assertEquals("Invalid username or password", thrown.getMessage());
	}

}
