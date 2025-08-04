//package fu.se.myplatform.api;
//
//import fu.se.myplatform.dto.MuteRequest;
//import fu.se.myplatform.service.ModerationService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/moderation")
//@RequiredArgsConstructor
//@PreAuthorize("hasRole('STAFF')")  // Chỉ STAFF mới có quyền truy cập các API này
//public class ModerationAPI {
//    private final ModerationService moderationService;
//
//    /**
//     * Mute một người dùng
//     * @param userId ID của người dùng bị mute
//     * @param request Thông tin mute (lý do, thời gian)
//     */
//    @PostMapping("/users/{userId}/mute")
//    public ResponseEntity<Void> muteUser(
//            @PathVariable Long userId,
//            @Valid @RequestBody MuteRequest request) {
//        moderationService.muteUser(userId, request);
//        return ResponseEntity.ok().build();
//    }
//
//    /**
//     * Unmute một người dùng
//     * @param userId ID của người dùng cần unmute
//     */
//    @PostMapping("/users/{userId}/unmute")
//    public ResponseEntity<Void> unmuteUser(@PathVariable Long userId) {
//        moderationService.unmuteUser(userId);
//        return ResponseEntity.ok().build();
//    }
//}
