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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final BlogRepository blogRepository;
    private final AccountService accountService;
    private final ModerationService moderationService;

    @Transactional
    public CommentResponse addComment(Long blogId, CommentRequest request) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new NotFoundException("Blog not found"));

        if (!blog.isAllowComments()) {
            throw new BadRequestException("Comments are disabled for this blog");
        }

        Account currentUser = accountService.getCurrentUser();

        if (moderationService.isUserMuted(currentUser.getUserId())) {
            throw new ForbiddenException("You are currently muted and cannot comment");
        }

        Comment comment = new Comment();
        comment.setContent(request.getContent());  // Bỏ phần xử lý UTF-8
        comment.setBlog(blog);
        comment.setUser(currentUser);

        Comment savedComment = commentRepository.save(comment);

        CommentResponse response = new CommentResponse();
        response.setId(savedComment.getId());
        response.setContent(savedComment.getContent());
        response.setBlogId(savedComment.getBlogId());
        response.setUserId(savedComment.getUserId());
        response.setUserName(savedComment.getUser().getUsername());
        response.setCreatedAt(savedComment.getCreatedAt());
        response.setDeleted(savedComment.isDeleted());

        return response;
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

        comment.softDelete();  // Thay vì xóa trực tiếp, dùng softDelete
        commentRepository.save(comment);
    }

//    @Transactional(readOnly = true)
//    public Page<CommentResponse> getComments(Long blogId, Pageable pageable) {
//        return commentRepository.findByBlogIdAndIsDeletedFalseOrderByCreatedAtDesc(blogId, pageable)
//                .map(comment -> {
//                    CommentResponse response = new CommentResponse();
//                    response.setId(comment.getId());
//                    response.setContent(comment.getContent());
//                    response.setBlogId(comment.getBlogId());
//                    response.setUserId(comment.getUserId());
//                    response.setUserName(comment.getUser().getUsername());
//                    response.setCreatedAt(comment.getCreatedAt());
//                    response.setDeleted(comment.isDeleted());
//                    return response;
//                });
//    }

    /**
     * Lấy comments cho tính năng load more
     * @param blogId ID của blog
     * @param page Số trang (bắt đầu từ 0)
     * @param size Số lượng comments mỗi lần load
     * @return Page chứa danh sách comments và thông tin phân trang
     */
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsForLoadMore(Long blogId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return commentRepository.findRootComments(blogId, pageable)
                .map(comment -> mapToCommentResponseWithReplies(comment, true));
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getRepliesForComment(Long commentId, int page, int size) {
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        Pageable pageable = PageRequest.of(page, size);
        return commentRepository.findRepliesPaged(commentId, pageable)
                .map(reply -> mapToCommentResponse(reply, parentComment.getUser()));
    }

    // Sửa lại phương thức mapToCommentResponseWithReplies để chỉ load 2 replies đầu tiên
    private CommentResponse mapToCommentResponseWithReplies(Comment comment, boolean loadReplies) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setBlogId(comment.getBlogId());
        response.setUserId(comment.getUserId());
        response.setUserName(comment.getUser().getUsername());
        response.setUserFullName(comment.getUser().getFullName());
        response.setCreatedAt(comment.getCreatedAt());
        response.setDeleted(comment.isDeleted());

        // Set thông tin về replies
        long totalReplies = commentRepository.countRepliesByCommentId(comment.getId());
        response.setCountReplies((int) totalReplies);
        response.setHasReplies(totalReplies > 0);

        // Nếu có replies, chỉ load 2 cái đầu tiên
        if (loadReplies && totalReplies > 0) {
            List<Comment> replies = commentRepository.findRepliesByCommentId(comment.getId()).stream()
                .limit(2)
                .collect(Collectors.toList());

            response.setReplies(
                replies.stream()
                    .map(reply -> mapToCommentResponse(reply, comment.getUser()))
                    .collect(Collectors.toList())
            );
        }

        return response;
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

    @Transactional
    public CommentResponse replyComment(Long blogId, Long parentCommentId, CommentRequest request) {
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new NotFoundException("Parent comment not found"));

        if (!parentComment.getBlogId().equals(blogId)) {
            throw new BadRequestException("Comment does not belong to this blog");
        }

        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new NotFoundException("Blog not found"));

        if (!blog.isAllowComments()) {
            throw new BadRequestException("Comments are disabled for this blog");
        }

        Account currentUser = accountService.getCurrentUser();

        if (moderationService.isUserMuted(currentUser.getUserId())) {
            throw new ForbiddenException("You are currently muted and cannot comment");
        }

        String mentionText = String.format("@%s (@%s) ",
            parentComment.getUser().getFullName(),
            parentComment.getUser().getUsername());

        String replyContent = mentionText + request.getContent();  // Bỏ phần xử lý UTF-8

        Comment reply = new Comment();
        reply.setContent(replyContent);
        reply.setBlog(blog);
        reply.setUser(currentUser);
        reply.setParentComment(parentComment);

        Comment savedReply = commentRepository.save(reply);

        return mapToCommentResponse(savedReply, parentComment.getUser());
    }

    // Helper method để map Comment sang CommentResponse với thông tin người được reply
    private CommentResponse mapToCommentResponse(Comment comment, Account replyToUser) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setBlogId(comment.getBlogId());
        response.setUserId(comment.getUserId());
        response.setUserName(comment.getUser().getUsername());  // Sửa getUserName() thành getUsername()
        response.setUserFullName(comment.getUser().getFullName());
        response.setCreatedAt(comment.getCreatedAt());
        response.setDeleted(comment.isDeleted());
        response.setParentId(comment.getParentId());

        if (replyToUser != null) {
            response.setReplyToUserFullName(replyToUser.getFullName());
            response.setReplyToUserName(replyToUser.getUsername());  // Sửa getUserName() thành getUsername()
            response.setMentionedText(String.format("@%s (@%s) ",
                replyToUser.getFullName(),
                replyToUser.getUsername()));  // Sửa getUserName() thành getUsername()
        }

        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            response.setReplies(
                comment.getReplies().stream()
                    .map(reply -> mapToCommentResponse(reply, comment.getUser()))
                    .collect(Collectors.toList())
            );
        }

        return response;
    }
}
