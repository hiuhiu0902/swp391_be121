// src/main/java/fu/se/myplatform/api/AssessmentAPI.java
package fu.se.myplatform.api;

import fu.se.myplatform.dto.FagerstromResultDTO;
import fu.se.myplatform.dto.FagerstromTestRequest;
import fu.se.myplatform.entity.Assessment;
import fu.se.myplatform.service.FagerstromService;
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

    @PostMapping
    public ResponseEntity<Assessment> performAssessment(@RequestBody FagerstromTestRequest request) {
        Assessment result = fagerstromService.performAndSaveAssessment(request);
        return ResponseEntity.ok(result); // Trả về Assessment object, có chứa ID
    }
}