package fu.se.myplatform.service;

import fu.se.myplatform.dto.CommentRequest;
import fu.se.myplatform.dto.CommentResponse;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.Blog;
import fu.se.myplatform.entity.Comment;
import fu.se.myplatform.enums.Role;
import fu.se.myplatform.exception.BadRequestException;
import fu.se.myplatform.exception.ForbiddenException;
import fu.se.myplatform.exception.NotFoundException;
import fu.se.myplatform.repository.BlogRepository;
import fu.se.myplatform.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final BlogRepository blogRepository;
    private final AccountService accountService;
    private final ModerationService moderationService;
    private final ModelMapper modelMapper;

    @Transactional
    public CommentResponse addComment(Long blogId, CommentRequest request) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new NotFoundException("Blog not found"));

        if (!blog.isAllowComments()) {
            throw new BadRequestException("Comments are disabled for this blog");
        }

        Account currentUser = accountService.getCurrentUser();

        // Kiểm tra xem user có bị mute không
        if (moderationService.isUserMuted(currentUser.getUserId())) {
            throw new ForbiddenException("You are currently muted and cannot comment");
        }

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setBlog(blog);
        comment.setUser(currentUser);

        return modelMapper.map(commentRepository.save(comment), CommentResponse.class);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        Account currentUser = accountService.getCurrentUser();

        // Chỉ STAFF hoặc chủ comment mới được xóa
        if (!currentUser.getRole().equals(Role.STAFF) && !comment.getUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException("You don't have permission to delete this comment");
        }

        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    public Page<CommentResponse> getComments(Long blogId, Pageable pageable) {
        return commentRepository.findByBlogIdAndIsDeletedFalseOrderByCreatedAtDesc(blogId, pageable)
                .map(comment -> modelMapper.map(comment, CommentResponse.class));
    }

    @Transactional
    public void toggleComments(Long blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new NotFoundException("Blog not found"));

        Account currentUser = accountService.getCurrentUser();

        // Chỉ STAFF hoặc chủ bài viết mới được bật/tắt comment
        if (!currentUser.getRole().equals(Role.STAFF) && !blog.getUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException("You don't have permission to toggle comments for this blog");
        }

        blog.setAllowComments(!blog.isAllowComments());
        blogRepository.save(blog);
    }

    public boolean isCommentOwner(Long commentId, String username) {
        return commentRepository.findById(commentId)
                .map(comment -> comment.getUser().getUsername().equals(username))
                .orElse(false);
    }
}
