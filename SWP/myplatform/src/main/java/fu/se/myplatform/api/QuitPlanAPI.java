package fu.se.myplatform.api;

import fu.se.myplatform.dto.QuitPlanRequest;
import fu.se.myplatform.dto.QuitPlanResponse;
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

    @PostMapping("/plans")
    public ResponseEntity<?> createPlan(@RequestBody QuitPlanRequest planRequest) {
        try {
            QuitPlanResponse response = quitPlanService.createPlan(planRequest);
            return ResponseEntity.ok(response);
        } catch (MyException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Đã xảy ra lỗi khi tạo kế hoạch, vui lòng thử lại sau");
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
