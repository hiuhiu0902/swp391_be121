package fu.se.myplatform.service;
import fu.se.myplatform.dto.*;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.Coach;
import fu.se.myplatform.entity.Member;
import fu.se.myplatform.entity.Staff;
import fu.se.myplatform.enums.Role;
import fu.se.myplatform.exception.BadRequestException;
import fu.se.myplatform.exception.exception.AuthenticationException;
import fu.se.myplatform.repository.AccountRepository;
import fu.se.myplatform.repository.AuthenticationRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    public Account register(Account account) {
        // Kiểm tra email đã tồn tại chưa
        if (authenticationRepository.findAccountByEmail(account.getEmail()) != null) {
            throw new BadRequestException("Email đã được sử dụng");
        }

        // Kiểm tra username đã tồn tại chưa
        if (authenticationRepository.findAccountByUserName(account.getUsername()) != null) {
            throw new BadRequestException("Tên đăng nhập đã được sử dụng");
        }
        if(account.getPhoneNumber() == null){
            throw new AuthenticationException("Số điện thoại không được để trống");
        }
        if(account.getGender() == null){
            throw new AuthenticationException("Giới tính không được để trống");
        }

        account.setPassword(passwordEncoder.encode(account.getPassword()));
        account.setRole(Role.MEMBER); // Set default role to MEMBER
        Account newaccount = authenticationRepository.save(account);

        // Tạo bản ghi Member tương ứng
        Member member = new Member();
        member.setUser(newaccount);
        member.setStatus("active");
        member.setIsVip(false);
        member.setIsActived(true);  // Set isActived explicitly
        member.setVipStartDate(null);
        member.setVipExpiryDate(null);
        memberRepository.save(member);

        try {
            EmailDetail emailDetail = new EmailDetail();
            emailDetail.setRecipient(account.getEmail());
            emailDetail.setSubject("Chào mừng đến với My Platform");
            emailDetail.setLink("http://localhost:3000/");
            emailService.sendWelcomeEmail(emailDetail);
        } catch (Exception e) {
            // Log lỗi nhưng không throw exception vì đây không phải lỗi nghiêm trọng
            logEventService.logError("Không thể gửi email chào mừng cho " + account.getEmail(), e.getMessage());
        }

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
            // Bước 1: Xác thực username và password
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getUserName(),
                    loginRequest.getPassword()
            ));
        } catch (Exception e) {
            // Ghi log lỗi đăng nhập (sai thông tin)
            logEventService.logError("Login failed for user: " + loginRequest.getUserName(), e.getMessage());
            logger.error("Login failed for user: {}", loginRequest.getUserName(), e);

            throw new AuthenticationException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        // Bước 2: Lấy thông tin tài khoản từ database
        Account account = authenticationRepository.findAccountByUserName(loginRequest.getUserName());

        if (!account.isActive()) {
            logEventService.logError("Attempted login to deactivated account: " + loginRequest.getUserName(), "Account is inactive");
            logger.warn("Attempted login to deactivated account: {}", loginRequest.getUserName());

            // Ném lỗi và không cho phép đăng nhập
            throw new AuthenticationException("Tài khoản của bạn đã bị khóa hoặc không tồn tại.");
        }

        // Bước 4: Nếu tài khoản hợp lệ và đang hoạt động, tiếp tục xử lý
        logEventService.logLogin(loginRequest.getUserName());
        logger.info("User {} logged in successfully", loginRequest.getUserName());

        AccountResponse accountResponse = modelMapper.map(account, AccountResponse.class);

        if (account.getRole() == Role.MEMBER) {
            Member member = memberRepository.findByUser(account);
            if (member != null) {
                accountResponse.setMemberId(member.getMemberId());
                accountResponse.setIsVip(member.getIsVip());
                accountResponse.setVipStartDate(member.getVipStartDate());
                accountResponse.setVipExpiryDate(member.getVipExpiryDate());
                accountResponse.setMemberStatus(member.getStatus());
                if (member.getCoach() != null) {
                    accountResponse.setCoachId(member.getCoach().getCoachId());
                    accountResponse.setCoachAddress(member.getCoach().getAddress());
                    accountResponse.setCoachStatus(member.getCoach().getStatus());
                }
            }
        } else if (account.getRole() == Role.COACH) {
            Coach coach = coachRepository.findByUser(account);
            if (coach != null) {
                accountResponse.setCoachId(coach.getCoachId());
                accountResponse.setCoachAddress(coach.getAddress());
                accountResponse.setCoachStatus(coach.getStatus());
            }
        }

        // Tạo token và trả về response
        String token = tokenService.generateToken(account);
        accountResponse.setToken(token);
        return accountResponse;
    }
    public ProfileResponse viewProfile(String username) {
        Account account = authenticationRepository.findAccountByUserName(username);
        Member member = memberRepository.findByUser(account);
        if (account == null) {
            throw new ResourceNotFoundException("Account not found");
        }

        // Check if user is viewing their own profile or has admin role
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!currentUsername.equals(username) && !hasAdminRole()) {
            throw new AccessDeniedException("Not authorized to view this profile");
        }
        ProfileResponse profileResponse = modelMapper.map(account, ProfileResponse.class);
        if(account.getRole() == Role.MEMBER) {
            profileResponse.setVip(member.getIsVip());
        }

        return profileResponse;
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
    public AccountResponse createSpecialAccount(CreateAccountRequest request) {
        Account account = new Account();
        account.setUserName(request.getUserName());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setFullName(request.getFullName());
        account.setEmail(request.getEmail());
        account.setPhoneNumber(request.getPhoneNumber());
        account.setRole(request.getRole());
        account.setGender(request.getGender());
        Account newAccount = authenticationRepository.save(account);
        if (request.getRole() != null) {
            switch (request.getRole()) {
                case MEMBER -> {
                    Member member = new Member();
                    member.setUser(newAccount);
                    member.setStatus(request.getStatus());
                    member.setIsVip(false);
                    member.setIsActived(true);  // Set isActived explicitly
                    member.setVipStartDate(null);
                    member.setVipExpiryDate(null);
                    memberRepository.save(member);
                }
                case STAFF -> {
                    fu.se.myplatform.entity.Staff staff = new fu.se.myplatform.entity.Staff();
                    staff.setUser(newAccount);
                    staff.setStatus(request.getStatus());
                    staffRepository.save(staff);
                }
                case COACH -> {
                    fu.se.myplatform.entity.Coach coach = new fu.se.myplatform.entity.Coach();
                    coach.setUser(newAccount);
                    coach.setStatus("ACTIVE");
                    coachRepository.save(coach);
                }
                default -> {}
            }
            emailService.sendAccountCreationNotification(request.getEmail(), request.getUserName(), request.getPassword());
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
        if (request.getRole() != null) {
            switch (request.getRole()) {
                case STAFF -> {
                    var staff = staffRepository.findByUser(account);
                    if (staff == null) {
                        staff = new Staff();          // <- Tên entity đúng như trong thư mục
                        staff.setUser(account);
                    }
                    if (request.getPosition() != null) staff.setPosition(request.getPosition());
                    if (request.getStatus() != null) staff.setStatus(request.getStatus());
                    staffRepository.save(staff);
                }
                case COACH -> {
                    var coach = coachRepository.findByUser(account);
                    if (coach == null) {
                        coach = new Coach();
                        coach.setUser(account);
                    }
                    if (request.getAddress() != null) coach.setAddress(request.getAddress());
                    if (request.getStatus() != null) coach.setStatus(request.getStatus());
                    coachRepository.save(coach);
                }
                case MEMBER -> {
                    var member = memberRepository.findByUser(account);
                    if (member == null) {
                        member = new Member();
                        member.setUser(account);
                        member.setIsVip(false);
                        member.setStatus("active");
                    }
                    if (request.getIsVip() != null) member.setIsVip(request.getIsVip());
                    if (request.getVipStartDate() != null) member.setVipStartDate(LocalDate.parse(request.getVipStartDate()));
                    if (request.getVipExpiryDate() != null) member.setVipExpiryDate(LocalDate.parse(request.getVipExpiryDate()));
                    if (request.getStatus() != null) member.setStatus(request.getStatus());
                    memberRepository.save(member);
                }
                default -> {}
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
                Member member = memberRepository.findByUser(account);
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
    @Transactional
    public void deleteAccount(Long userId) {
        try {
            Account accountToDelete = authenticationRepository.findById(userId)
                    .orElseThrow(() -> new AuthenticationException("Account not found"));

            Account currentAdmin = getCurrentAccount();
            if(!currentAdmin.getRole().equals(Role.ADMIN)){
                throw new AccessDeniedException("Bạn không có quyền xóa tài khoản này");
            }
            if(currentAdmin.getUsername().equals(accountToDelete.getUsername())){
                throw new BadRequestException("Admim không thể xóa chính mình");
            }
            switch (accountToDelete.getRole()) {
                case MEMBER:
                    Member member = memberRepository.findByUser(accountToDelete);
                    if (member != null) {
                        member.setIsActived(false);
                        member.setStatus("DEACTIVATED");
                        memberRepository.save(member);
                        member.setCoach(null);
                    }
                    break;
                case COACH:
                    Coach coach = coachRepository.findByUser(accountToDelete);
                    if(coach != null) {
                        List<Member> asssignedMembers = new ArrayList<>(coach.getMembers());
                        for (Member assignedMember :
                                asssignedMembers) {
                            String memberEmail = assignedMember.getUser().getEmail();
                            String memberName = assignedMember.getUser().getFullName();
                            emailService.sendCoachDeletedNotification(memberEmail, memberName, coach.getUser().getFullName());

                            assignedMember.setCoach(null); // Gỡ liên kết với coach
                        }
                        memberRepository.saveAll(asssignedMembers);

                        coach.setStatus("INACTIVE");
                        coachRepository.save(coach);
                    }
                    break;
                case STAFF:
                    Staff staff = staffRepository.findByUser(accountToDelete);
                    if(staff != null) {
                        staff.setStatus("INACTIVE");
                        staffRepository.save(staff);
                    }
                    break;
                default:
                    break;
            }

            accountToDelete.setActive(false);
            emailService.sendAccountLockedNotification(accountToDelete.getEmail(), accountToDelete.getUsername());
            authenticationRepository.save(accountToDelete);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa tài khoản: " + e.getMessage(), e);
        }
    }
    public Account resetPassword(ResetPasswordRequest resetPasswordRequest) {
        Account account = getCurrentAccount();
        account.setPassword(passwordEncoder.encode(resetPasswordRequest.getPassword()));
        return  accountRepository.save(account);
    }
    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        try {
            Account account = authenticationRepository.findAccountByEmail(forgotPasswordRequest.getEmail());
            if (account == null) {
                throw new ResourceNotFoundException("Không tìm thấy tài khoản với email này");
            }

            String resetToken = tokenService.generateToken(account);
            EmailDetail emailDetail = new EmailDetail();
            emailDetail.setRecipient(account.getEmail());
            emailDetail.setSubject("Yêu cầu đặt lại mật khẩu");
            emailDetail.setLink("http://localhost:3000/reset-password?token=" + resetToken);

            try {
                emailService.sendMail(emailDetail);
            } catch (Exception e) {
                logEventService.logError("Lỗi gửi email đặt lại mật khẩu cho " + account.getEmail(), e.getMessage());
                throw new RuntimeException("Không thể gửi email đặt lại mật khẩu. Vui lòng thử lại sau.");
            }
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logEventService.logError("Lỗi xử lý yêu cầu đặt lại mật khẩu", e.getMessage());
            throw new RuntimeException("Đã xảy ra lỗi khi xử lý yêu cầu. Vui lòng thử lại sau.");
        }
    }

    public List<AccountResponse> getAccountsByRole(String role) {
        Role roleEnum = Role.valueOf(role);
        List<Account> accounts;

        switch (roleEnum) {
            case MEMBER:
                accounts = accountRepository.findByRole(Role.MEMBER);
                break;
            case STAFF:
                accounts = accountRepository.findByRole(Role.STAFF);
                break;
            case COACH:
                accounts = accountRepository.findByRole(Role.COACH);

                break;
            default:
                throw new BadRequestException("Invalid role");
        }

        return accounts.stream()
                .map(account -> {
                    AccountResponse dto = modelMapper.map(account, AccountResponse.class);

                    // For MEMBER role, fetch member info to get status
                    if (roleEnum == Role.MEMBER) {
                        Member member = memberRepository.findByUser_UserId(account.getUserId());
                        if (member != null) {
                            dto.setStatus(member.getStatus());
                            dto.setMemberId(member.getMemberId());
                            dto.setIsVip(member.getIsVip());
                        }
                    }else if (roleEnum == Role.COACH) {
                        Coach coach = coachRepository.findByUser(account);
                        if (coach != null) {
                            dto.setStatus(coach.getStatus());
                        }


                    }else{
                        Staff staff = staffRepository.findByUser(account);
                        dto.setStatus(staff.getStatus());
                    }

                    // For STAFF or COACH, you can do similar fetches if needed

                    return dto;
                })
                .toList();
    }

    /**
     * Đếm số tài khoản theo role
     */
    public long countAccountsByRole(String role) {
        return accountRepository.countByRole(Role.valueOf(role.toUpperCase()));
    }

    /**
     * Đếm số tài khoản mới trong n ngày gần nhất
     */
    public long countNewUsersInLastDays(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return accountRepository.countByCreatedAtAfter(startDate);
    }
}
