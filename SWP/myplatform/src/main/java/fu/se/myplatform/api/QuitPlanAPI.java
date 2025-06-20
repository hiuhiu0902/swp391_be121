package fu.se.myplatform.api;

import fu.se.myplatform.dto.QuitPlanRequest;
import fu.se.myplatform.dto.QuitPlanResponse;
<<<<<<< HEAD
=======
import fu.se.myplatform.dto.QuitPlanRequest;
import fu.se.myplatform.dto.QuitPlanResponse;
>>>>>>> 4a1f5c0c39b43b62e434892413b2abeb7b8f2c9c
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

<<<<<<< HEAD
    @DeleteMapping("/plans")
    public ResponseEntity<Void> deleteCurrentUserPlan() {
        quitPlanService.deleteCurrentUserPlan();
=======
    @DeleteMapping("/plans/{planId}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long planId) {
        quitPlanService.deletePlan(planId);
>>>>>>> 4a1f5c0c39b43b62e434892413b2abeb7b8f2c9c
        return ResponseEntity.noContent().build();
    }
}
