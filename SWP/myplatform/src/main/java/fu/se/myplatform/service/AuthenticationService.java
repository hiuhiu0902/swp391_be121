package fu.se.myplatform.service;
import fu.se.myplatform.dto.ProfileResponse;
import fu.se.myplatform.dto.ProfileRequest;
import fu.se.myplatform.dto.AccountResponse;
import fu.se.myplatform.dto.EmailDetail;
import fu.se.myplatform.dto.LoginRequest;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.enums.Role;
import fu.se.myplatform.exception.exception.AuthenticationException;
import fu.se.myplatform.repository.AuthenticationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import fu.se.myplatform.exception.exception.ResourceNotFoundException;
import java.beans.Encoder;

@Service
public class AuthenticationService implements UserDetailsService {
    @Autowired
    AuthenticationRepository authenticationRepository;
    // Implement methods for authentication, such as register and login
    @Autowired
    AuthenticationManager authenticationManager;
// You can add methods for user registration, login, etc.
    @Autowired
    TokenService tokenService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EmailService emailService;
    public Account register(Account account) {
        account.password = passwordEncoder.encode(account.getPassword());
        account.setRole(Role.MEMBER); // Set default role to USER
        Account newaccount = authenticationRepository.save(account);

        EmailDetail emailDetail = new EmailDetail();
        emailDetail.setRecipient(account.email);
        emailDetail.setSubject("Welcome to My Platform");
        emailService.sendMail(emailDetail);
        return newaccount;
    }

    public AccountResponse login(LoginRequest loginRequest) {
        try {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                        loginRequest.getUserName(),
                        loginRequest.getPassword()
                ));
        }catch (Exception e) {
            System.out.println("Thông tin đăng nhập không chính xác");
            throw new AuthenticationException("Invalid username or password");
        }
        Account account = authenticationRepository.findAccountByUserName(loginRequest.getUserName());
        AccountResponse accountResponse = modelMapper.map(account, AccountResponse.class);
        String token = tokenService.generateToken(account);
        accountResponse.setToken(token);
        return accountResponse;
    }
    public ProfileResponse viewProfile(String username) {
        Account account = authenticationRepository.findAccountByUserName(username);
        if (account == null) {
            throw new ResourceNotFoundException("Account not found");
        }

        // Check if user is viewing their own profile or has admin role
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!currentUsername.equals(username) && !hasAdminRole()) {
            throw new AccessDeniedException("Not authorized to view this profile");
        }

        return modelMapper.map(account, ProfileResponse.class);
    }

    public ProfileResponse updateProfile(String username, ProfileRequest profileRequest) {
        Account account = authenticationRepository.findAccountByUserName(username);
        if (account == null) {
            throw new ResourceNotFoundException("Account not found");
        }

        // Check if user is updating their own profile
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!currentUsername.equals(username)) {
            throw new AccessDeniedException("Not authorized to update this profile");
        }

        // Update only allowed fields
        account.setFullName(profileRequest.getFullName());
        account.setEmail(profileRequest.getEmail());
        account.setPhoneNumber(profileRequest.getPhone());

        Account updatedAccount = authenticationRepository.save(account);
        return modelMapper.map(updatedAccount, ProfileResponse.class);
    }

    private boolean hasAdminRole() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return authenticationRepository.findAccountByUserName(username);
    }
}
