package fu.se.myplatform.service;

import fu.se.myplatform.entity.Account;
import fu.se.myplatform.enums.Role;
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
		// Mock repository save to add to list
		when(authenticationRepository.save(any(Account.class))).thenAnswer(invocation -> {
			Account acc = invocation.getArgument(0);
			accounts.add(acc);
			return acc;
		});
	}

	@Test
	void register_shouldAddAccountToList() {
		Account account = new Account();
		account.setEmail("test@example.com");
		account.setPassword("password");
		account.setRole(Role.MEMBER);

		Account saved = authenticationService.register(account);

		assertEquals(1, accounts.size());
		assertEquals("test@example.com", accounts.get(0).getEmail());
		assertEquals(saved, accounts.get(0));
	}
}