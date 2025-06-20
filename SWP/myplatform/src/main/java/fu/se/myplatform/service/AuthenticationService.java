package fu.se.myplatform.service;
import fu.se.myplatform.dto.*;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.enums.Role;
import fu.se.myplatform.exception.exception.AuthenticationException;
import fu.se.myplatform.repository.AccountRepository;
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
import java.util.List;

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
    AccountRepository accountRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EmailService emailService;
    @Autowired
    fu.se.myplatform.repository.MemberRepository memberRepository;
    @Autowired
    fu.se.myplatform.repository.StaffRepository staffRepository;
    @Autowired
    fu.se.myplatform.repository.CoachRepository coachRepository;
    @Autowired
    LogEventService logEventService;
    public Account register(Account account) {
        account.password = passwordEncoder.encode(account.getPassword());
        account.setRole(Role.MEMBER); // Set default role to MEMBER
        Account newaccount = authenticationRepository.save(account);

        // Tạo bản ghi Member tương ứng
        fu.se.myplatform.entity.Member member = new fu.se.myplatform.entity.Member();
        member.setUser(newaccount);
        member.setStatus("active");
        member.setIsVip(false);
        member.setVipStartDate(null);
        member.setVipExpiryDate(null);
        memberRepository.save(member);

        EmailDetail emailDetail = new EmailDetail();
        emailDetail.setRecipient(account.email);
        emailDetail.setSubject("Welcome to My Platform");
        emailService.sendMail(emailDetail);
        // Ghi log đăng ký
        logEventService.logRegister(account.getUsername());
        return newaccount;
    }

    public Account getCurrentAccount() {
        Account account =(Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return accountRepository.findByUserName(account.getUsername());
    }
    public AccountResponse login(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getUserName(),
                    loginRequest.getPassword()
            ));
        } catch (Exception e) {
            // Ghi log lỗi đăng nhập
            logEventService.logError("Login failed for user: " + loginRequest.getUserName(), e.getMessage());
            System.out.println("Thông tin đăng nhập không chính xác");
            throw new AuthenticationException("Invalid username or password");
        }
        Account account = authenticationRepository.findAccountByUserName(loginRequest.getUserName());
        // Ghi log đăng nhập thành công
        logEventService.logLogin(loginRequest.getUserName());
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
    public AccountResponse createSpecialAccount(fu.se.myplatform.dto.CreateAccountRequest request) {
        Account account = new Account();
        account.setUserName(request.getUserName());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setFullName(request.getFullName());
        account.setEmail(request.getEmail());
        account.setPhoneNumber(request.getPhoneNumber());
        account.setRole(request.getRole());
        Account newAccount = authenticationRepository.save(account);
        // Tạo entity theo role
        if (request.getRole() != null) {
            switch (request.getRole()) {
//                case MEMBER -> {
//                    fu.se.myplatform.entity.Member member = new fu.se.myplatform.entity.Member();
//                    member.setUser(newAccount);
//                    member.setStatus(request.getStatus());
//                    member.setIsVip(false);
//                    member.setVipStartDate(null);
//                    member.setVipExpiryDate(null);
//                    memberRepository.save(member);
//                }
                case STAFF -> {
                    fu.se.myplatform.entity.Staff staff = new fu.se.myplatform.entity.Staff();
                    staff.setUser(newAccount);
                    staff.setStatus(request.getStatus());
                    staffRepository.save(staff);
                }
                case COACH -> {
                    fu.se.myplatform.entity.Coach coach = new fu.se.myplatform.entity.Coach();
                    coach.setUser(newAccount);
                    coach.setStatus(request.getStatus());
                    coachRepository.save(coach);
                }
                default -> {}
            }
        }
        return modelMapper.map(newAccount, AccountResponse.class);
    }
    public AccountResponse updateAccountByAdmin(Long userId, fu.se.myplatform.dto.UpdateAccountRequest request) {
        Account account = authenticationRepository.findById(userId)
                .orElseThrow(() -> new fu.se.myplatform.exception.exception.ResourceNotFoundException("Account not found"));
        if (request.getFullName() != null) account.setFullName(request.getFullName());
        if (request.getEmail() != null) account.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null) account.setPhoneNumber(request.getPhoneNumber());
        if (request.getGender() != null) account.setGender(request.getGender());
        if (request.getRole() != null) account.setRole(request.getRole());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            account.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        // Nếu có status riêng cho từng role, bạn có thể cập nhật ở đây
        Account updated = authenticationRepository.save(account);

        // Update Staff
        if (request.getPosition() != null) {
            fu.se.myplatform.entity.Staff staff = staffRepository.findByUser(account);
            if (staff != null) {
                staff.setPosition(request.getPosition());
                staffRepository.save(staff);
            }
        }
        // Update Coach
        if (request.getAddress() != null) {
            fu.se.myplatform.entity.Coach coach = coachRepository.findByUser(account);
            if (coach != null) {
                coach.setAddress(request.getAddress());
                coachRepository.save(coach);
            }
        }
        // Update Member (nếu cần)
        if (request.getIsVip() != null || request.getVipStartDate() != null || request.getVipExpiryDate() != null) {
            fu.se.myplatform.entity.Member member = memberRepository.findByUser(account);
            if (member != null) {
                if (request.getIsVip() != null) member.setIsVip(request.getIsVip());
                if (request.getVipStartDate() != null) member.setVipStartDate(java.time.LocalDate.parse(request.getVipStartDate()));
                if (request.getVipExpiryDate() != null) member.setVipExpiryDate(java.time.LocalDate.parse(request.getVipExpiryDate()));
                memberRepository.save(member);
            }
        }
        return modelMapper.map(updated, AccountResponse.class);
    }
    public List<AccountResponse> getAllAccounts() {
        List<Account> accounts = authenticationRepository.findAll();
        return accounts.stream()
                .map(account -> modelMapper.map(account, AccountResponse.class))
                .toList();
    }
    public AccountResponse getAccountDetail(Long userId) {
        Account account = authenticationRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        AccountResponse response = modelMapper.map(account, AccountResponse.class);
        // Lấy thông tin riêng từng loại
        switch (account.getRole()) {
            case MEMBER -> {
                fu.se.myplatform.entity.Member member = memberRepository.findByUser(account);
                if (member != null) {
                    response.setIsVip(member.getIsVip());
                    response.setVipStartDate(member.getVipStartDate());
                    response.setVipExpiryDate(member.getVipExpiryDate());
                    response.setMemberStatus(member.getStatus());
                }
            }
            case STAFF -> {
                fu.se.myplatform.entity.Staff staff = staffRepository.findByUser(account);
                if (staff != null) {
                    response.setStaffPosition(staff.getPosition());
                    response.setStaffStatus(staff.getStatus());
                }
            }
            case COACH -> {
                fu.se.myplatform.entity.Coach coach = coachRepository.findByUser(account);
                if (coach != null) {
                    response.setCoachAddress(coach.getAddress());
                    response.setCoachStatus(coach.getStatus());
                }
            }
            default -> {}
        }
        return response;
    }
    public void deleteAccount(Long userId) {
        Account account = authenticationRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        // Xóa các entity liên quan nếu cần
        memberRepository.deleteByUser(account);
        staffRepository.deleteByUser(account);
        coachRepository.deleteByUser(account);
        authenticationRepository.delete(account);
    }
    public Account resetPassword(ResetPasswordRequest resetPasswordRequest) {
        Account account = getCurrentAccount();
        account.setPassword(passwordEncoder.encode(resetPasswordRequest.getPassword()));
        return  accountRepository.save(account);
    }
    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        Account account = authenticationRepository.findAccountByEmail(forgotPasswordRequest.getEmail());
        if (account == null) {
            throw new UsernameNotFoundException("User not found");
        } else {

            EmailDetail emailDetail = new EmailDetail();
            emailDetail.setRecipient(account.email);
            emailDetail.setSubject("Forgot Password Request");
            emailDetail.setLink("http://localhost:8080/reset-password?token=" + tokenService.generateToken(account));
            emailService.sendMail(emailDetail);

        }
    }
}
