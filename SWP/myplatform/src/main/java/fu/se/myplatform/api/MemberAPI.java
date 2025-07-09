package fu.se.myplatform.api;

import fu.se.myplatform.entity.Member;
import fu.se.myplatform.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/member")
public class MemberAPI {

    @Autowired
    private MemberService memberService;

    @GetMapping("/{memberId}")
    public ResponseEntity<Member> getMemberProfile(@PathVariable Long memberId) {
        Member member = memberService.getMemberProfile(memberId);
        return ResponseEntity.ok(member);
    }

    @PostMapping("/{memberId}/avatar")
    public ResponseEntity<?> updateMemberImage(
            @PathVariable Long memberId,
            @RequestParam("file") MultipartFile file) {
        try {
            memberService.updateProfileImage(memberId, file);
            return ResponseEntity.ok("Cập nhật ảnh đại diện thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{memberId}/avatar")
    public ResponseEntity<?> getMemberAvatarBase64(@PathVariable Long memberId) {
        String base64 = memberService.getProfileImageBase64(memberId);
        return ResponseEntity.ok(base64);
    }

    @PutMapping("/{memberId}/coach/{coachId}")
    public ResponseEntity<?> assignCoach(@PathVariable Long memberId, @PathVariable Long coachId) {
        try {
            memberService.assignCoach(memberId, coachId);
            return ResponseEntity.ok("Đã chọn huấn luyện viên thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
