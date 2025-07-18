package fu.se.myplatform.api;

import fu.se.myplatform.dto.BlogRequest;
import fu.se.myplatform.dto.BlogResponse;
import fu.se.myplatform.enums.BlogCategory;
import fu.se.myplatform.exception.BadRequestException;
import fu.se.myplatform.exception.ForbiddenException;
import fu.se.myplatform.exception.NotFoundException;
import fu.se.myplatform.service.BlogService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
@SecurityRequirement(
        name = "api"
)
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
     * @return Danh sách bài viết theo điều kiện, sắp xếp mới nhất lên đầu
     */
    @GetMapping
    public ResponseEntity<List<BlogResponse>> getAllBlogs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BlogCategory category,
            @RequestParam(required = false) Boolean featured) {
        return ResponseEntity.ok(blogService.getAllBlogs(search, category, featured));
    }

    /**
     * Lấy danh sách bài viết của người dùng hiện tại
     * Role: STAFF, COACH, MEMBER, ADMIN
     */
    @GetMapping("/my-blogs")
    @PreAuthorize("hasAnyRole('STAFF', 'COACH', 'MEMBER', 'ADMIN')")
    public ResponseEntity<List<BlogResponse>> getMyBlogs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BlogCategory category) {
        return ResponseEntity.ok(blogService.getMyBlogs(search, category));
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
     * Search blogs - Tìm kiếm blog theo từ khóa và category
     * Kết quả mặc định sắp xếp theo thời gian mới nhất
     */
    @GetMapping("/search")
    public ResponseEntity<List<BlogResponse>> searchBlogs(
            @RequestParam String keyword,
            @RequestParam(required = false) BlogCategory category) {
        return ResponseEntity.ok(blogService.getAllBlogs(keyword, category, null));
    }

    /**
     * Lấy feed bài viết, sắp xếp theo mức độ tương tác
     * Public API - Không cần đăng nhập
     * @param page Số trang (bắt đầu từ 0)
     * @param size Số lượng bài viết mỗi trang (mặc định 10)
     * @param category Lọc theo danh mục (optional)
     * @param featured Lọc bài viết nổi bật (optional)
     * @return Danh sách bài viết đã sắp xếp theo mức độ tương tác
     */
    @GetMapping("/feed")
    public ResponseEntity<Map<String, Object>> getBlogFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) BlogCategory category,
            @RequestParam(required = false) Boolean featured) {

        return ResponseEntity.ok(blogService.getBlogFeed(category, page, size, featured));
    }

    /**
     * Like bài viết
     * Role: STAFF, COACH, MEMBER, ADMIN
     * @param id ID bài viết
     * @return Thông tin bài viết sau khi được like
     */
    @PostMapping("/{id}/like")
    @PreAuthorize("hasAnyRole('STAFF', 'COACH', 'MEMBER', 'ADMIN')")
    public ResponseEntity<BlogResponse> likeBlog(@PathVariable Long id) {
        return ResponseEntity.ok(blogService.likeBlog(id));
    }

    /**
     * Unlike bài viết
     * Role: STAFF, COACH, MEMBER, ADMIN
     * @param id ID bài viết
     * @return Thông tin bài viết sau khi bị unlike
     */
    @PostMapping("/{id}/unlike")
    @PreAuthorize("hasAnyRole('STAFF', 'COACH', 'MEMBER', 'ADMIN')")
    public ResponseEntity<BlogResponse> unlikeBlog(@PathVariable Long id) {
        return ResponseEntity.ok(blogService.unlikeBlog(id));
    }

    /**
     * Initialize columns - Khởi tạo các cột likes và view_count
     * Role: ADMIN
     */
    @PostMapping("/initialize-columns")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> initializeColumns() {
        blogService.initializeColumns();
        return ResponseEntity.ok("Columns initialized successfully");
    }

    /**
     * Upload ảnh cho blog
     * Role: STAFF, COACH, MEMBER, ADMIN
     * @param file File ảnh cần upload
     * @return URL của ảnh trên Cloudinary
     */
//    @PostMapping("/upload-image")
//    @PreAuthorize("hasAnyRole('STAFF', 'COACH', 'MEMBER', 'ADMIN')")
//    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
//        String imageUrl = blogService.uploadImage(file);
//        return ResponseEntity.ok(Map.of("url", imageUrl));
//    }
}
