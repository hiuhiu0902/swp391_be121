package fu.se.myplatform.api;

import fu.se.myplatform.dto.CommentRequest;
import fu.se.myplatform.dto.CommentResponse;
import fu.se.myplatform.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blogs/{blogId}/comments")
@RequiredArgsConstructor
public class CommentAPI {
    private final CommentService commentService;

    /**
     * Thêm comment vào bài viết
     * Role: Tất cả user đã đăng nhập (không bị mute)
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long blogId,
            @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.addComment(blogId, request));
    }

    /**
     * Xóa comment
     * Role: STAFF hoặc chính người comment
     */
    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasRole('STAFF') or @commentService.isCommentOwner(#commentId, authentication.principal)")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long blogId,
            @PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }

    /**
     * Lấy danh sách comment của bài viết
     * Public API
     */
    @GetMapping
    public ResponseEntity<Page<CommentResponse>> getComments(
            @PathVariable Long blogId,
            Pageable pageable) {
        return ResponseEntity.ok(commentService.getComments(blogId, pageable));
    }

    /**
     * Bật/tắt comment cho bài viết
     * Role: STAFF hoặc chủ bài viết
     */
    @PatchMapping("/toggle")
    @PreAuthorize("hasRole('STAFF') or @blogService.isBlogOwner(#blogId, authentication.principal)")
    public ResponseEntity<Void> toggleComments(@PathVariable Long blogId) {
        commentService.toggleComments(blogId);
        return ResponseEntity.ok().build();
    }
}
