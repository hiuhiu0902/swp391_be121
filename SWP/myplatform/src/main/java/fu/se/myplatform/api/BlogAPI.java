package fu.se.myplatform.api;

import fu.se.myplatform.dto.BlogRequest;
import fu.se.myplatform.dto.BlogResponse;
import fu.se.myplatform.enums.BlogCategory;
import fu.se.myplatform.service.BlogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogAPI {
    private final BlogService blogService;

    /**
     * Tạo bài viết mới
     * Role: STAFF, COACH, MEMBER, ADMIN
     * @param request Thông tin bài viết (title, content, thumbnail, category)
     * @return Thông tin bài viết đã tạo
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'COACH', 'MEMBER', 'ADMIN')")
    public ResponseEntity<BlogResponse> createBlog(@Valid @RequestBody BlogRequest request) {
        return ResponseEntity.ok(blogService.createBlog(request));
    }

    /**
     * Cập nhật bài viết
     * Role: STAFF có thể sửa tất cả bài viết
     * Các role khác chỉ được sửa bài viết của mình
     * @param id ID bài viết cần sửa
     * @param request Thông tin cập nhật
     * @return Thông tin bài viết sau khi cập nhật
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'COACH', 'MEMBER', 'ADMIN')")
    public ResponseEntity<BlogResponse> updateBlog(
            @PathVariable Long id,
            @Valid @RequestBody BlogRequest request) {
        return ResponseEntity.ok(blogService.updateBlog(id, request));
    }

    /**
     * Xóa bài viết
     * Role: STAFF có thể xóa tất cả bài viết
     * Các role khác chỉ được xóa bài viết của mình
     * @param id ID bài viết cần xóa
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'COACH', 'MEMBER', 'ADMIN')")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Xem chi tiết bài viết
     * Public API - Không cần đăng nhập
     * @param id ID bài viết
     * @return Thông tin chi tiết bài viết
     */
    @GetMapping("/{id}")
    public ResponseEntity<BlogResponse> getBlog(@PathVariable Long id) {
        return ResponseEntity.ok(blogService.getBlog(id));
    }

    /**
     * Tìm kiếm và lọc bài viết, mặc định sắp xếp theo thời gian mới nhất
     * Public API - Không cần đăng nhập
     * @param search Tìm kiếm theo title hoặc content
     * @param category Lọc theo danh mục
     * @param featured Lọc bài viết nổi bật
     * @param pageable Phân trang (page, size, sort)
     * @return Danh sách bài viết theo điều kiện, sắp xếp mới nhất lên đầu
     */
    @GetMapping
    public ResponseEntity<Page<BlogResponse>> getAllBlogs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BlogCategory category,
            @RequestParam(required = false) Boolean featured,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(blogService.getAllBlogs(search, category, featured, pageable));
    }

    /**
     * Xem danh sách bài viết của người dùng hiện tại
     * Role: STAFF, COACH, MEMBER, ADMIN
     * @param search Tìm kiếm theo title hoặc content
     * @param category Lọc theo danh mục
     * @param pageable Phân trang (page, size, sort)
     * @return Danh sách bài viết của người dùng
     */
    @GetMapping("/my-blogs")
    @PreAuthorize("hasAnyRole('STAFF', 'COACH', 'MEMBER', 'ADMIN')")
    public ResponseEntity<Page<BlogResponse>> getMyBlogs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BlogCategory category,
            Pageable pageable) {
        return ResponseEntity.ok(blogService.getMyBlogs(search, category, pageable));
    }

    /**
     * Đánh dấu/bỏ đánh dấu bài viết nổi bật
     * Role: Chỉ STAFF mới có quyền
     * @param id ID bài viết
     * @return Thông tin bài viết sau khi cập nhật
     */
    @PatchMapping("/{id}/feature")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<BlogResponse> toggleFeatured(@PathVariable Long id) {
        return ResponseEntity.ok(blogService.toggleFeatured(id));
    }

    /**
     * Lấy danh sách bài viết với infinite scroll
     * Public API - Không cần đăng nhập
     * @param lastId ID của bài viết cuối cùng đã load (null nếu là load lần đầu)
     * @param limit Số lượng bài viết muốn lấy (mặc định 10)
     * @param category Lọc theo danh mục (optional)
     * @param featured Lọc bài viết nổi bật (optional)
     * @return Danh sách bài viết mới, sắp xếp theo thời gian mới nhất
     */
    @GetMapping("/feed")
    public ResponseEntity<List<BlogResponse>> getBlogFeed(
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) BlogCategory category,
            @RequestParam(required = false) Boolean featured) {
        return ResponseEntity.ok(blogService.getBlogFeed(lastId, limit, category, featured));
    }
}
