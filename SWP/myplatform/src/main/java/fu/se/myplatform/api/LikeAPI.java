package fu.se.myplatform.api;

import fu.se.myplatform.service.BlogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blogs/{blogId}/likes")
@RequiredArgsConstructor
@Tag(name = "Like API")
@SecurityRequirement(
        name = "api"
)public class LikeAPI {
    private final BlogService blogService;

    /**
     * Lấy số lượt like của bài viết
     * Role: Public API
     */
    @GetMapping
    public ResponseEntity<Long> getLikeCount(@PathVariable Long blogId) {
        return ResponseEntity.ok(blogService.getBlogLikeCount(blogId));
    }

    /**
     * Like/Unlike bài viết
     * Role: Tất cả user đã đăng nhập
     */
    @PostMapping
    public ResponseEntity<Void> likeBlog(@PathVariable Long blogId) {
        blogService.toggleLike(blogId);
        return ResponseEntity.ok().build();
    }
}
