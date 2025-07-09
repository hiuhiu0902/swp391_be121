package fu.se.myplatform.api;

import fu.se.myplatform.dto.AccountResponse;
import fu.se.myplatform.dto.CreateAccountRequest;
import fu.se.myplatform.service.AuthenticationService;
import fu.se.myplatform.service.LogReportService;
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

    // 2. Thống kê số lượt đăng nhập/đăng ký theo thời gian (giả sử đã có log)
    @GetMapping("/report/logins")
    public ResponseEntity<Map<String, Object>> getLoginRegisterStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("loginCount", logReportService.getLoginCount());
        stats.put("registerCount", logReportService.getRegisterCount());
        return ResponseEntity.ok(stats);
    }

    // 4. Thống kê lỗi hệ thống (giả sử đã có log)
    @GetMapping("/report/errors")
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
}
