package fu.se.myplatform.service;

import fu.se.myplatform.dto.BlogRequest;
import fu.se.myplatform.dto.BlogResponse;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.Blog;
import fu.se.myplatform.entity.BlogLike;
import fu.se.myplatform.enums.BlogCategory;
import fu.se.myplatform.enums.Role;
import fu.se.myplatform.exception.ForbiddenException;
import fu.se.myplatform.exception.NotFoundException;
import fu.se.myplatform.repository.BlogRepository;
import fu.se.myplatform.repository.BlogLikeRepository;
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
    private final BlogLikeRepository blogLikeRepository;
    @Autowired
    private CloudinaryService cloudinaryService;

    @Transactional
    public BlogResponse createBlog(BlogRequest request, MultipartFile file) {
        Account currentUser = accountService.getCurrentUser();

        Blog blog = new Blog();
        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blog.setThumbnail(request.getThumbnail());
        blog.setCategory(request.getCategory());
        blog.setUser(currentUser);
        blog.setPublished(request.isPublished());

        // Upload ảnh nếu có và lưu URL vào blog
        if (file != null && !file.isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadBlogImage(file);
                blog.setImage(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Không thể upload ảnh: " + e.getMessage());
            }
        }

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
    public List<BlogResponse> getAllBlogs(String keyword, BlogCategory category, Boolean featured) {
        try {
            Account currentUser = accountService.getCurrentUser();
            List<Blog> blogs;
            if (featured != null) {
                blogs = blogRepository.findBySearchCriteriaAndFeatured(keyword, category, featured);
            } else {
                if (category != null) {
                    blogs = blogRepository.findBlogFeed(category.name(), 100, null);
                } else {
                    blogs = blogRepository.findBlogFeed(null, 100, null);
                }
            }

            return blogs.stream()
                    .map(blog -> {
                        BlogResponse response = modelMapper.map(blog, BlogResponse.class);
                        response.setLikes(blog.getLikes());
                        // Kiểm tra xem user hiện tại đã like bài viết này chưa
                        if (currentUser != null) {
                            response.setLiked(blogLikeRepository.existsByBlogIdAndUserId(blog.getId(), currentUser.getUserId()));
                        }
                        return response;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
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
        try {
            Account currentUser = accountService.getCurrentUser();
            Pageable pageable = PageRequest.of(page, size);

            Page<Blog> blogPage = blogRepository.findBlogFeedPaged(
                    category != null ? category.name() : null,
                    featured,
                    pageable);

            List<BlogResponse> blogResponses = blogPage.getContent().stream()
                    .map(blog -> {
                        BlogResponse response = modelMapper.map(blog, BlogResponse.class);
                        response.setLikes(blog.getLikes());
                        // Kiểm tra xem user hiện tại đã like bài viết này chưa
                        if (currentUser != null) {
                            response.setLiked(blogLikeRepository.existsByBlogIdAndUserId(blog.getId(), currentUser.getUserId()));
                        }
                        return response;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("content", blogResponses);
            response.put("currentPage", blogPage.getNumber());
            response.put("totalItems", blogPage.getTotalElements());
            response.put("totalPages", blogPage.getTotalPages());
            response.put("hasNext", blogPage.hasNext());

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                "content", List.of(),
                "currentPage", 0,
                "totalItems", 0,
                "totalPages", 0,
                "hasNext", false
            );
        }
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
    public BlogResponse toggleLike(Long blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new NotFoundException("Blog not found"));

        Account currentUser = accountService.getCurrentUser();

        // Kiểm tra xem user đã like blog này chưa
        boolean hasLiked = blogLikeRepository.existsByBlogIdAndUserId(blogId, currentUser.getUserId());

        if (hasLiked) {
            // Nếu đã like thì unlike
            blogLikeRepository.deleteByBlogIdAndUserId(blogId, currentUser.getUserId());
            blog.setLikes(blog.getLikes() - 1);
        } else {
            // Nếu chưa like thì like
            BlogLike blogLike = new BlogLike();
            blogLike.setBlog(blog);
            blogLike.setUser(currentUser);
            blogLikeRepository.save(blogLike);
            blog.setLikes(blog.getLikes() + 1);
        }

        blog = blogRepository.save(blog);
        BlogResponse response = modelMapper.map(blog, BlogResponse.class);
        response.setLikes(blog.getLikes());
        response.setLiked(!hasLiked); // Đảo ngược trạng thái like
        return response;
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

    /**
     * Upload ảnh cho blog
     * @param file File ảnh cần upload
     * @param blogId ID của blog cần thêm ảnh
     * @return URL của ảnh từ Cloudinary
     */
    @Transactional
    public String uploadImage(MultipartFile file, Long blogId) {
        try {
            // Kiểm tra blog tồn tại
            Blog blog = blogRepository.findById(blogId)
                    .orElseThrow(() -> new NotFoundException("Blog not found"));

            // Kiểm tra quyền - chỉ chủ sở hữu hoặc STAFF mới được upload
            Account currentUser = accountService.getCurrentUser();
            if (!currentUser.getRole().equals(Role.STAFF) && !blog.getUserId().equals(currentUser.getUserId())) {
                throw new ForbiddenException("You don't have permission to update this blog");
            }

            // Upload ảnh lên Cloudinary
            String imageUrl = cloudinaryService.uploadImage(file, "blogs");

            // Cập nhật URL ảnh vào blog
            blog.setImage(imageUrl);
            blogRepository.save(blog);

            return imageUrl;
        } catch (IOException e) {
            throw new RuntimeException("Không thể upload ảnh: " + e.getMessage());
        }
    }
}
