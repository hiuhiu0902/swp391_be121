package fu.se.myplatform.api;

import fu.se.myplatform.dto.QuitPlanRequest;
import fu.se.myplatform.dto.QuitPlanResponse;
import fu.se.myplatform.dto.SystemPlanRequestDTO;
import fu.se.myplatform.dto.UserPlanRequestDTO;
import fu.se.myplatform.repository.QuitPlanRepository;
import fu.se.myplatform.service.QuitPlanService;
import fu.se.myplatform.exception.BadRequestException;
import fu.se.myplatform.exception.MyException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name = "api")
@RequestMapping("/api")
public class QuitPlanAPI {
    @Autowired
    private QuitPlanService quitPlanService;

    @PostMapping("/system-generated")
    public ResponseEntity<?> createSystemPlan(@RequestBody SystemPlanRequestDTO request) {
        try {
            return ResponseEntity.ok(quitPlanService.createSystemGeneratedPlan(request));
        } catch (MyException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi khi tạo kế hoạch theo hệ thống.");
        }
    }

    /**
     * API để tạo kế hoạch do người dùng tùy chỉnh.
     */
    @PostMapping("/user-defined")
    public ResponseEntity<?> createUserPlan(@RequestBody UserPlanRequestDTO request) {
        try {
            return ResponseEntity.ok(quitPlanService.createUserDefinedPlan(request));
        } catch (MyException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi khi tạo kế hoạch tùy chỉnh.");
        }
    }
    @GetMapping("/plans")
    public ResponseEntity<?> viewPlan() {
        try {
            QuitPlanResponse response = quitPlanService.getCurrentUserPlan();
            return ResponseEntity.ok(response);
        } catch (MyException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/plans")
    public ResponseEntity<?> deletePlan() {
        try {
            quitPlanService.deleteCurrentUserPlan();
            return ResponseEntity.noContent().build();
        } catch (MyException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Đã xảy ra lỗi khi xóa kế hoạch, vui lòng thử lại sau");
        }
    }
}
