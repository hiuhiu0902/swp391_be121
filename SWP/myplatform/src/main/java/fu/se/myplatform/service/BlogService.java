package fu.se.myplatform.service;

import fu.se.myplatform.dto.BlogRequest;
import fu.se.myplatform.dto.BlogResponse;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.Blog;
import fu.se.myplatform.enums.BlogCategory;
import fu.se.myplatform.enums.Role;
import fu.se.myplatform.exception.ForbiddenException;
import fu.se.myplatform.exception.NotFoundException;
import fu.se.myplatform.repository.BlogRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogService {
    private final BlogRepository blogRepository;
    private final AccountService accountService;
    private final ModelMapper modelMapper;
    @Autowired
    private CloudinaryService cloudinaryService;

    @Transactional
    public BlogResponse createBlog(BlogRequest request) {
        Account currentUser = accountService.getCurrentUser();

        Blog blog = new Blog();
        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blog.setThumbnail(request.getThumbnail());
        blog.setCategory(request.getCategory());
        blog.setUser(currentUser);
        blog.setPublished(request.isPublished());

        return modelMapper.map(blogRepository.save(blog), BlogResponse.class);
    }

    @Transactional
    public BlogResponse updateBlog(Long id, BlogRequest request) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blog not found"));

        // Chỉ cho phép Staff hoặc chính người tạo sửa bài viết
        Account currentUser = accountService.getCurrentUser();
        if (!currentUser.getRole().equals(Role.STAFF) && !blog.getUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException("You don't have permission to update this blog");
        }

        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blog.setThumbnail(request.getThumbnail());
        blog.setCategory(request.getCategory());
        blog.setPublished(request.isPublished());

        return modelMapper.map(blogRepository.save(blog), BlogResponse.class);
    }

    @Transactional
    public void deleteBlog(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blog not found"));

        Account currentUser = accountService.getCurrentUser();
        if (!currentUser.getRole().equals(Role.STAFF) && !blog.getUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException("You don't have permission to delete this blog");
        }

        blogRepository.delete(blog);
    }

    @Transactional
    public BlogResponse likeBlog(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blog not found"));
        blogRepository.incrementLikes(id);
        return modelMapper.map(blog, BlogResponse.class);
    }

    @Transactional
    public BlogResponse unlikeBlog(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blog not found"));
        blogRepository.decrementLikes(id);
        return modelMapper.map(blog, BlogResponse.class);
    }

    @Transactional(readOnly = true)
    public BlogResponse getBlog(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blog not found"));
        // Tăng view count khi có người xem blog
        blogRepository.incrementViewCount(id);
        return modelMapper.map(blog, BlogResponse.class);
    }

    @Transactional(readOnly = true)
    public List<BlogResponse> getAllBlogs(String search, BlogCategory category, Boolean featured) {
        if (featured != null) {
            return blogRepository.findBySearchCriteriaAndFeatured(search, category, featured)
                    .stream()
                    .map(blog -> modelMapper.map(blog, BlogResponse.class))
                    .collect(Collectors.toList());
        } else {
            // Khi không có category được chọn, trả về tất cả blog
            List<Blog> blogs = category != null ?
                blogRepository.findBlogFeed(category.name(), 100, null) :
                blogRepository.findBlogFeed(null, 100, null);
            return blogs.stream()
                    .map(blog -> modelMapper.map(blog, BlogResponse.class))
                    .collect(Collectors.toList());
        }
    }

    public List<BlogResponse> getMyBlogs(String search, BlogCategory category) {
        Account currentUser = accountService.getCurrentUser();
        return blogRepository.findByUserIdAndSearchCriteria(currentUser.getUserId(), search, category)
                .stream()
                .map(blog -> modelMapper.map(blog, BlogResponse.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public BlogResponse toggleFeatured(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blog not found"));
        blog.setFeatured(!blog.isFeatured());
        return modelMapper.map(blogRepository.save(blog), BlogResponse.class);
    }

    public Map<String, Object> getBlogFeed(BlogCategory category, int page, int size, Boolean featured) {
        // Tạo Pageable để phân trang
        Pageable pageable = PageRequest.of(page, size);

        // Lấy danh sách blog có phân trang
        Page<Blog> blogPage = blogRepository.findBlogFeedPaged(
                category != null ? category.name() : null,
                featured,
                pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", blogPage.getContent().stream()
                .map(blog -> modelMapper.map(blog, BlogResponse.class))
                .collect(Collectors.toList()));
        response.put("currentPage", blogPage.getNumber());
        response.put("totalItems", blogPage.getTotalElements());
        response.put("totalPages", blogPage.getTotalPages());
        response.put("hasNext", blogPage.hasNext());

        return response;
    }

    public boolean isBlogOwner(Long blogId, String username) {
        return blogRepository.findById(blogId)
                .map(blog -> blog.getUser().getUsername().equals(username))
                .orElse(false);
    }

    /**
     * Đếm tổng số bài blog
     */
    public long countBlogs() {
        return blogRepository.count();
    }

    /**
     * Đếm số blog theo category
     */
    public Map<BlogCategory, Long> countBlogsByCategory() {
        Map<BlogCategory, Long> counts = new HashMap<>();
        for (BlogCategory category : BlogCategory.values()) {
            counts.put(category, blogRepository.countByCategory(category));
        }
        return counts;
    }

    /**
     * Search blogs - Tìm kiếm blog theo từ khóa và category
     * Trả về danh sách đã sắp xếp theo thời gian mới nhất
     */
    public List<BlogResponse> searchBlogs(String keyword, BlogCategory category) {
        return blogRepository.findBySearchCriteriaAndFeatured(keyword, category, null)
                .stream()
                .map(blog -> modelMapper.map(blog, BlogResponse.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void incrementViewCount(Long blogId) {
        // Kiểm tra blog tồn tại
        if (!blogRepository.existsById(blogId)) {
            throw new NotFoundException("Blog not found");
        }
        blogRepository.incrementViewCount(blogId);
    }

    @Transactional
    public void toggleLike(Long blogId) {
        // Kiểm tra blog tồn tại
        if (!blogRepository.existsById(blogId)) {
            throw new NotFoundException("Blog not found");
        }
        blogRepository.incrementLikes(blogId);
    }

    public Map<String, Long> getBlogStats(BlogCategory category) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", blogRepository.countByCategory(category));
        stats.put("published", blogRepository.countByCategoryAndPublishedTrue(category));
        stats.put("featured", blogRepository.countByCategoryAndFeaturedTrue(category));
        return stats;
    }

    public List<BlogResponse> getFeaturedBlogs(BlogCategory category, int limit) {
        return blogRepository.findByCategoryAndFeaturedTrue(category, PageRequest.of(0, limit))
                .stream()
                .map(blog -> modelMapper.map(blog, BlogResponse.class))
                .collect(Collectors.toList());
    }

    public List<BlogResponse> getMostEngagedBlogs(BlogCategory category, int limit) {
        return blogRepository.findMostEngagedBlogs(category, Pageable.ofSize(limit))
                .stream()
                .map(blog -> modelMapper.map(blog, BlogResponse.class))
                .collect(Collectors.toList());
    }

    @Modifying
    @Query(value = "ALTER TABLE blog ADD view_count bigint DEFAULT 0 NOT NULL", nativeQuery = true)
    @Transactional
    public void addViewCountColumn() {
        // Method này sẽ thực thi câu lệnh SQL trực tiếp
    }

    @Transactional
    public void initializeColumns() {
        try {
            blogRepository.addLikesColumn();
        } catch (Exception e) {
            // Column might already exist
        }
        try {
            blogRepository.addViewCountColumn();
        } catch (Exception e) {
            // Column might already exist
        }
    }

    @Transactional(readOnly = true)
    public Long getBlogLikeCount(Long blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new NotFoundException("Blog not found"));
        return blog.getLikes();
    }

//    public String uploadImage(MultipartFile file) {
//        try {
//            Map<String, String> options = new HashMap<>();
//            options.put("folder", "blogs");
//            options.put("resource_type", "auto");
//            return cloudinaryService.upload(file, options);
//        } catch (IOException e) {
//            throw new RuntimeException("Could not upload image", e);
//        }
//    }
}
