// src/main/java/fu/se/myplatform/api/AssessmentAPI.java
package fu.se.myplatform.api;

import fu.se.myplatform.dto.FagerstromResultDTO;
import fu.se.myplatform.dto.FagerstromTestRequest;
import fu.se.myplatform.entity.Assessment;
import fu.se.myplatform.service.FagerstromService;
import fu.se.myplatform.service.QuitPlanService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// src/main/java/fu/se/myplatform/api/AssessmentAPI.java
@RestController
@RequestMapping("/api/assessments")
@SecurityRequirement(name = "api")
public class AssessmentAPI {
    @Autowired FagerstromService fagerstromService;
    @Autowired
    QuitPlanService quitPlanService;
    @PostMapping
    public ResponseEntity<Assessment> performAssessment(@RequestBody FagerstromTestRequest request) {
        Assessment result = fagerstromService.performAndSaveAssessment(request);
        return ResponseEntity.ok(result); // Trả về Assessment object, có chứa ID
    }

    @GetMapping("/check/{userId}")
    public ResponseEntity<Boolean> hasAssessment(@PathVariable Long userId) {
        boolean hasAssessment = fagerstromService.hasAssessment(userId);
        return ResponseEntity.ok(hasAssessment);
    }
    @GetMapping
    public ResponseEntity<FagerstromResultDTO> getAssessment() {
        Assessment result = fagerstromService.getAssessment();
        long durationWeek = quitPlanService.determinePlanDuration(result.getDependencyLevel());
        FagerstromResultDTO fagerstromResultDTO = new FagerstromResultDTO(result.getScore(),result.getDependencyLevel(),durationWeek);
        return ResponseEntity.ok(fagerstromResultDTO);
    }
}