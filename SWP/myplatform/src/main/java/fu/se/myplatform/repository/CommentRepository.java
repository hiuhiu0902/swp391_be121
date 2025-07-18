package fu.se.myplatform.repository;

import fu.se.myplatform.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Lấy các comment gốc (không phải reply)
    @Query("SELECT c FROM Comment c WHERE c.blog.id = :blogId AND c.parentComment IS NULL AND c.isDeleted = false")
    Page<Comment> findRootComments(@Param("blogId") Long blogId, Pageable pageable);

    // Lấy replies của một comment
    @Query(value = "SELECT * FROM Comment WHERE parent_id = :commentId AND is_deleted = 0 ORDER BY created_at ASC", nativeQuery = true)
    List<Comment> findRepliesByCommentId(@Param("commentId") Long commentId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.parentComment.id = :commentId AND c.isDeleted = false")
    long countRepliesByCommentId(@Param("commentId") Long commentId);

    @Query(value = "SELECT * FROM Comment WHERE parent_id = :commentId AND is_deleted = 0 ORDER BY created_at ASC",
           countQuery = "SELECT COUNT(*) FROM Comment WHERE parent_id = :commentId AND is_deleted = 0",
           nativeQuery = true)
    Page<Comment> findRepliesPaged(@Param("commentId") Long commentId, Pageable pageable);
}
