package fu.se.myplatform.api;

import fu.se.myplatform.service.AuthenticationService;
import fu.se.myplatform.service.LogReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

@RestController
@RequestMapping("/api/admin/report")
public class AdminReportAPI {
    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    LogReportService logReportService;

    // 2. Thống kê số lượt đăng nhập/đăng ký theo thời gian (giả sử đã có log)
    @GetMapping("/logins")
    public ResponseEntity<Map<String, Object>> getLoginRegisterStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("loginCount", logReportService.getLoginCount());
        stats.put("registerCount", logReportService.getRegisterCount());
        return ResponseEntity.ok(stats);
    }

    // 4. Thống kê lỗi hệ thống (giả sử đã có log)
    @GetMapping("/errors")
    public ResponseEntity<Map<String, Object>> getErrorStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("errorCount", logReportService.getErrorCount());

        return ResponseEntity.ok(stats);
    }
}
