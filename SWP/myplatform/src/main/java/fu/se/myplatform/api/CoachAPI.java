package fu.se.myplatform.api;

import fu.se.myplatform.dto.MemberShortDTO;
import fu.se.myplatform.entity.Coach;
import fu.se.myplatform.service.CoachService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coaches")  // Định nghĩa đường dẫn API
@RequiredArgsConstructor
@SecurityRequirement(
        name = "api"
)
public class CoachAPI {

    private final CoachService coachService;

    // API để lấy danh sách các Member đã được phân cho Coach
    @GetMapping("/{coachId}/members")
    public ResponseEntity<List<MemberShortDTO>> getMembersAssignedToCoach(@PathVariable Long coachId) {
        List<MemberShortDTO> members = coachService.getMembersAssignedToCoach(coachId);
        return ResponseEntity.ok(members);  // Trả về danh sách MemberShortDTO
    }

}
