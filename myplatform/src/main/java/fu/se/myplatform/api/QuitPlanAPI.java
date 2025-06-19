package fu.se.myplatform.api;

import fu.se.myplatform.dto.PlanRequest;
import fu.se.myplatform.dto.PlanResponse;
import fu.se.myplatform.service.QuitPlanService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SecurityRequirement(
        name = "api"
)
@RequestMapping("/api/plan")
public class QuitPlanAPI {
    @Autowired
    private QuitPlanService quitPlanService;

    @PostMapping
    public ResponseEntity<PlanResponse> createPlan(@RequestBody PlanRequest planRequest) {
        PlanResponse response = quitPlanService.createPlan(planRequest);
        return ResponseEntity.ok(response);
    }
}
