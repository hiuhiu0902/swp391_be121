package fu.se.myplatform.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fu.se.myplatform.dto.BlogRequest;
import fu.se.myplatform.dto.BlogResponse;
import fu.se.myplatform.enums.BlogCategory;
import fu.se.myplatform.exception.BadRequestException;
import fu.se.myplatform.exception.ForbiddenException;
import fu.se.myplatform.exception.NotFoundException;
import fu.se.myplatform.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
     * Request body dạng form-data gồm các trường:
     * - title: Tiêu đề bài viết
     * - content: Nội dung bài viết
     * - category: QUIT_JOURNEY
     * - published: true/false
     * - file: File ảnh cho blog
     * @return Thông tin bài viết đã tạo
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STAFF', 'COACH', 'MEMBER', 'ADMIN')")
    public ResponseEntity<?> createBlog(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("category") String category,
            @RequestParam("published") Boolean published,
            @RequestParam("file") MultipartFile file) {
        try {
            // Validate file first
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn ảnh cho bài viết"));
            }

            // Validate other fields
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập tiêu đề bài viết"));
            }
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập nội dung bài viết"));
            }
            if (published == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn trạng thái xuất bản"));
            }

            // Validate category
            BlogCategory blogCategory;
            try {
                blogCategory = BlogCategory.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error",
                    "Danh mục không hợp lệ. Các danh mục hợp lệ: QUIT_JOURNEY, SUCCESS_STORY, EXPERIENCE, MOTIVATION, CHALLENGE, LIFE_STORY"));
            }

            BlogRequest request = new BlogRequest();
            request.setTitle(title);
            request.setContent(content);
            request.setCategory(blogCategory);
            request.setPublished(published);

            BlogResponse response = blogService.createBlog(request, file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi khi tạo blog: " + e.getMessage()));
        }
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
     * @param keyword Từ khóa tìm kiếm trong title hoặc content
     * @param category Lọc theo danh mục
     * @param featured Lọc bài viết nổi bật
     * @return Danh sách bài viết theo điều kiện, sắp xếp mới nhất lên đầu
     */
    @GetMapping
    public ResponseEntity<List<BlogResponse>> getAllBlogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BlogCategory category,
            @RequestParam(required = false) Boolean featured) {
        return ResponseEntity.ok(blogService.getAllBlogs(keyword, category, featured));
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
     * Toggle like bài viết (like/unlike)
     * Role: STAFF, COACH, MEMBER, ADMIN
     * Hoạt động như nút like Facebook:
     * - Click lần đầu: like (tăng count)
     * - Click lần nữa: unlike (giảm count)
     * @param id ID bài viết
     * @return Thông tin bài viết sau khi toggle like
     */
    @PostMapping("/{id}/toggle-like")
    @PreAuthorize("hasAnyRole('STAFF', 'COACH', 'MEMBER', 'ADMIN')")
    public ResponseEntity<?> toggleLikeBlog(@PathVariable Long id) {
        try {
            BlogResponse response = blogService.toggleLike(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
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
     * @param id ID của blog cần thêm ảnh
     * @return URL của ảnh trên Cloudinary
     */
    @Operation(summary = "Upload ảnh cho blog",
            description = "Upload ảnh cho một blog cụ thể. Ảnh sẽ được lưu trên Cloudinary.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Upload thành công",
                    content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = UploadResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Lỗi validation hoặc upload",
                    content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))})
    })
    @PostMapping(value = "/{id}/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STAFF', 'COACH', 'MEMBER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> uploadImage(
            @Parameter(description = "ID của blog", required = true)
            @PathVariable Long id,
            @Parameter(description = "File ảnh cho blog (jpg, png, etc.)", required = true)
            @RequestParam("file") MultipartFile file) {
        String imageUrl = blogService.uploadImage(file, id);
        return ResponseEntity.ok(Map.of("url", imageUrl));
    }

    /**
     * Search blogs
     * Tìm kiếm blog theo keyword và category
     * @param keyword từ khóa tìm kiếm (không bắt buộc)
     * @param category danh mục blog (không bắt buộc)
     * @return danh sách blog tìm thấy
     */
    @GetMapping("/search")
    public ResponseEntity<List<BlogResponse>> searchBlogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BlogCategory category) {
        try {
            List<BlogResponse> blogs = blogService.searchBlogs(keyword, category);
            return ResponseEntity.ok(blogs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    @Schema(name = "UploadResponse")
    private static class UploadResponse {
        @Schema(example = "Upload ảnh thành công")
        public String message;
        @Schema(example = "https://cloudinary.com/...")
        public String url;
    }

    @Schema(name = "ErrorResponse")
    @Getter
    @AllArgsConstructor
    private static class ErrorResponse {
        @Schema(example = "Lỗi khi upload ảnh")
        private String message;
    }
}
