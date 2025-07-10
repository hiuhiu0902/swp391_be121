package fu.se.myplatform.api;

import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.Member;
import fu.se.myplatform.repository.AccountRepository;
import fu.se.myplatform.service.AccountService;
import fu.se.myplatform.service.MemberService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/member")
@SecurityRequirement(
        name = "api"
)
public class MemberAPI {

    @Autowired
    MemberService memberService;

    @Autowired
    AccountRepository  accountRepository;
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

    @PutMapping("/coach/{coachId}")
    public ResponseEntity<?> assignCoach(@PathVariable Long coachId) {
        try {
        // Lấy username đang đăng nhập
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // Lấy Account từ username
        Account account = accountRepository.findByUserName(username);
        if (account == null) {
            return ResponseEntity.status(401).body("Tài khoản không tồn tại hoặc chưa đăng nhập!");
        }

        // Gọi service mới với account & coachId
        Member updatedMember = memberService.assignCoach(account, coachId);

        // Bạn có thể trả về thông tin member, hoặc chỉ trả thông báo thành công
        // return ResponseEntity.ok(modelMapper.map(updatedMember, MemberResponse.class));
        return ResponseEntity.ok("Đã chọn huấn luyện viên thành công");
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
    }
}
