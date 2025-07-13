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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogService {
    private final BlogRepository blogRepository;
    private final AccountService accountService;
    private final ModelMapper modelMapper;

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

    public BlogResponse getBlog(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blog not found"));
        return modelMapper.map(blog, BlogResponse.class);
    }

    public Page<BlogResponse> getAllBlogs(String search, BlogCategory category, Boolean featured, Pageable pageable) {
        return blogRepository.findBySearchCriteriaAndFeatured(search, category, featured, pageable)
                .map(blog -> modelMapper.map(blog, BlogResponse.class));
    }

    public Page<BlogResponse> getMyBlogs(String search, BlogCategory category, Pageable pageable) {
        Account currentUser = accountService.getCurrentUser();
        return blogRepository.findByUserIdAndSearchCriteria(currentUser.getUserId(), search, category, pageable)
                .map(blog -> modelMapper.map(blog, BlogResponse.class));
    }

    @Transactional
    public BlogResponse toggleFeatured(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blog not found"));
        blog.setFeatured(!blog.isFeatured());
        return modelMapper.map(blogRepository.save(blog), BlogResponse.class);
    }

    public List<BlogResponse> getBlogFeed(Long lastId, int limit, BlogCategory category, Boolean featured) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        return blogRepository.findBlogFeed(lastId, category, featured, pageRequest)
                .getContent()
                .stream()
                .map(blog -> modelMapper.map(blog, BlogResponse.class))
                .collect(Collectors.toList());
    }

    public boolean isBlogOwner(Long blogId, String username) {
        return blogRepository.findById(blogId)
                .map(blog -> blog.getUser().getUsername().equals(username))
                .orElse(false);
    }
}
