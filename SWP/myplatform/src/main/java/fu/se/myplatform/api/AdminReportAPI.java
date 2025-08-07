package fu.se.myplatform.api;

import fu.se.myplatform.dto.AccountResponse;
import fu.se.myplatform.dto.CreateAccountRequest;
import fu.se.myplatform.service.AuthenticationService;
import fu.se.myplatform.service.BlogService;
import fu.se.myplatform.service.LogReportService;
import fu.se.myplatform.service.QuitPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * API Report cho Admin:
 * 1. /api/admin/report/users - Dashboard: Thống kê tổng số user, số user đang có kế hoạch cai thuốc theo từng gói XXXX Thiếu
 * 2. /api/admin/report/logins - Thống kê số lượt đăng nhập/đăng ký theo thời gian
 * 3. /api/admin/report/notifications - Lịch sử gửi thông báo/email XXXX Thiếu
 * 4. /api/admin/report/errors - Thống kê lỗi hệ thống
 *
 */
//hahaaaa
@RestController
@RequestMapping("/api/admin")
public class AdminReportAPI {
    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    LogReportService logReportService;

    @Autowired
    QuitPlanService quitPlanService;

    @Autowired
    private BlogService blogService;

    // 2. Thống kê số lượt đăng nhập/đăng ký theo thời gian (giả sử đã có log)
    @GetMapping("/report/logins")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getLoginRegisterStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("loginCount", logReportService.getLoginCount());
        stats.put("registerCount", logReportService.getRegisterCount());
        return ResponseEntity.ok(stats);
    }

    // 4. Thống kê lỗi hệ thống (giả sử đã có log)
    @GetMapping("/report/errors")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getErrorStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("errorCount", logReportService.getErrorCount());

        return ResponseEntity.ok(stats);
    }
    @PostMapping("/create-account")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> createSpecialAccount(@RequestBody CreateAccountRequest request) {
        AccountResponse newAccount = authenticationService.createSpecialAccount(request);
        return ResponseEntity.ok(newAccount);
    }
    @PutMapping("/update-account/{userId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> updateAccountByAdmin(
            @PathVariable Long userId,
            @RequestBody fu.se.myplatform.dto.UpdateAccountRequest request) {
        AccountResponse updated = authenticationService.updateAccountByAdmin(userId, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/accounts")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        List<AccountResponse> accounts = authenticationService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }


    @GetMapping("/account/{userId}/detail")
    @Operation(summary = "View account detail", description = "Get detail information for an account, including createdAt")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AccountResponse> getAccountDetail(@PathVariable Long userId) {
        AccountResponse account = authenticationService.getAccountDetail(userId);
        return ResponseEntity.ok(account);
    }
    @DeleteMapping("/account/{userId}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long userId) {
        authenticationService.deleteAccount(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/accounts/members")
    @Operation(summary = "Get all MEMBER accounts", description = "Get list of all accounts with MEMBER role")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponse>> getAllMemberAccounts() {
        List<AccountResponse> accounts = authenticationService.getAccountsByRole("MEMBER");
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/accounts/staff")
    @Operation(summary = "Get all STAFF accounts", description = "Get list of all accounts with STAFF role")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponse>> getAllStaffAccounts() {
        List<AccountResponse> accounts = authenticationService.getAccountsByRole("STAFF");
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/accounts/coaches")
    @Operation(summary = "Get all COACH accounts", description = "Get list of all accounts with COACH role")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponse>> getAllCoachAccounts() {
        List<AccountResponse> accounts = authenticationService.getAccountsByRole("COACH");
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard statistics", description = "Get overview statistics for admin dashboard")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. User Statistics - Thống kê người dùng
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("totalMembers", authenticationService.countAccountsByRole("MEMBER"));
        userStats.put("totalStaff", authenticationService.countAccountsByRole("STAFF"));
        userStats.put("totalCoaches", authenticationService.countAccountsByRole("COACH"));
        userStats.put("newUsersThisWeek", authenticationService.countNewUsersInLastDays(7));
        userStats.put("newUsersThisMonth", authenticationService.countNewUsersInLastDays(30));
        stats.put("userStats", userStats);

        // 2. System Activity - Hoạt động hệ thống
        Map<String, Object> activityStats = new HashMap<>();
        activityStats.put("totalLogins", logReportService.getLoginCount());
        activityStats.put("totalRegistrations", logReportService.getRegisterCount());
        activityStats.put("totalErrors", logReportService.getErrorCount());

        // Thống kê theo thời gian
        activityStats.put("loginLast7Days", logReportService.getLoginStatsByDay(7));
        activityStats.put("loginLast30Days", logReportService.getLoginStatsByDay(30));
        activityStats.put("registerLast7Days", logReportService.getRegistrationStatsByDay(7));
        activityStats.put("registerLast30Days", logReportService.getRegistrationStatsByDay(30));

        // Biểu đồ tăng trưởng theo tháng
        activityStats.put("growthByMonth", logReportService.getRegistrationStatsByMonth(12));

        stats.put("activityStats", activityStats);

        // 3. Service Statistics - Thống kê dịch vụ
        Map<String, Object> serviceStats = new HashMap<>();
        serviceStats.put("activeQuitPlans", quitPlanService.countActivePlans());

        // Blog statistics
        Map<String, Object> blogStats = new HashMap<>();
        blogStats.put("totalBlogs", blogService.countBlogs());
        blogStats.put("blogsByCategory", blogService.countBlogsByCategory());
        serviceStats.put("blogStats", blogStats);

        stats.put("serviceStats", serviceStats);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/report/dependency-stats")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getDependencyLevelStats() {
        Map<String, Long> stats = quitPlanService.getUserCountByDependencyLevel();
        return ResponseEntity.ok(stats);
    }
}
