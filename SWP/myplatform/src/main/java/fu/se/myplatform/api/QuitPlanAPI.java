package fu.se.myplatform.api;

import fu.se.myplatform.dto.QuitPlanRequest;
import fu.se.myplatform.dto.QuitPlanResponse;
import fu.se.myplatform.service.QuitPlanService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(
        name = "api"
)
@RequestMapping("/api")
public class QuitPlanAPI {
    @Autowired
    private QuitPlanService quitPlanService;

    @PostMapping("/plans")
    public ResponseEntity<QuitPlanResponse> createPlan(@RequestBody QuitPlanRequest planRequest) {
        QuitPlanResponse response = quitPlanService.createPlan(planRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/plans")
    public ResponseEntity<QuitPlanResponse> viewPlan() {
        try {
            QuitPlanResponse response = quitPlanService.getCurrentUserPlan();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/plans")
    public ResponseEntity<Void> deletePlan() {
        quitPlanService.deleteCurrentUserPlan();
        return ResponseEntity.noContent().build();
    }
}
