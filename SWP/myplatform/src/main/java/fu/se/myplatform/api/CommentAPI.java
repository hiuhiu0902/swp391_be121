package fu.se.myplatform.api;

import fu.se.myplatform.dto.CommentRequest;
import fu.se.myplatform.dto.CommentResponse;
import fu.se.myplatform.service.CommentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(
        name = "api"
)

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
     * Reply một comment
     * Role: Tất cả user đã đăng nhập (không bị mute)
     */
    @PostMapping("/{commentId}/reply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> replyComment(
            @PathVariable Long blogId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.replyComment(blogId, commentId, request));
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
     * Load comment với tính năng load more
     * Public API
     */
    @GetMapping("/load-more")
    public ResponseEntity<Page<CommentResponse>> loadMoreComments(
            @PathVariable Long blogId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(commentService.getCommentsForLoadMore(blogId, page, size));
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

    /**
     * Load more replies cho một comment cụ thể
     * Public API
     */
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<Page<CommentResponse>> loadMoreReplies(
            @PathVariable Long blogId,
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(commentService.getRepliesForComment(commentId, page, size));
    }
}
