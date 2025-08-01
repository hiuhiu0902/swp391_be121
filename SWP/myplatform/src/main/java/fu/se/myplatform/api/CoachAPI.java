package fu.se.myplatform.api;

import fu.se.myplatform.dto.*;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.QuitPlan;
import fu.se.myplatform.service.AccountService;
import fu.se.myplatform.service.CoachService;
import fu.se.myplatform.service.QuitPlanService;
import fu.se.myplatform.service.SmokingRecordService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
@RequestMapping("/api/coach")
public class CoachAPI {

    private final CoachService coachService;

    // API để lấy danh sách các Member đã được phân cho Coach
    @GetMapping("/{coachId}/members")
    public ResponseEntity<List<MemberShortDTO>> getMembersAssignedToCoach(@PathVariable Long coachId) {
        List<MemberShortDTO> members = coachService.getMembersAssignedToCoach(coachId);
        return ResponseEntity.ok(members);  // Trả về danh sách MemberShortDTO
    }
    @GetMapping("/{coachId}/members/{memberId}/records")
//    @PreAuthorize("#coachId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<List<SmokingRecordResponse>> getMemberSmokingRecords(
            @PathVariable Long coachId,
            @PathVariable Long memberId) {
        List<SmokingRecordResponse> records = coachService.getMemberSmokingRecords(coachId, memberId);
        return ResponseEntity.ok(records);
    }
    @GetMapping("/{coachId}/members/{memberId}/progress/week/{weekNumber}")
//    @PreAuthorize("#coachId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<WeeklyProgressStats> getMemberWeeklyProgress(
            @PathVariable Long coachId,
            @PathVariable Long memberId,
            @PathVariable int weekNumber) {
        WeeklyProgressStats stats = coachService.getMemberWeeklyProgress(coachId, memberId, weekNumber);
        return ResponseEntity.ok(stats);
    }

    /**
     * [COACH] API để xem tiến trình tất cả các tuần của một member.
     */
    @GetMapping("/{coachId}/members/{memberId}/progress/all-weeks")
//    @PreAuthorize("#coachId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<List<WeeklyProgressStats>> getMemberAllWeeksProgress(
            @PathVariable Long coachId,
            @PathVariable Long memberId) {
        List<WeeklyProgressStats> allStats = coachService.getMemberAllWeeksProgress(coachId, memberId);
        return ResponseEntity.ok(allStats);
    }
    @PatchMapping("/{coachId}/members/{memberId}/plan/next-week")
    public ResponseEntity<?> adjustNextWeekTarget(
            @PathVariable Long coachId,
            @PathVariable Long memberId,
            @RequestBody AdjustWeeklyTargetDTO request) {
        try {
            QuitPlanResponse updatedPlan = coachService.adjustNextWeekTarget(coachId, memberId, request);
            return ResponseEntity.ok(updatedPlan);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
