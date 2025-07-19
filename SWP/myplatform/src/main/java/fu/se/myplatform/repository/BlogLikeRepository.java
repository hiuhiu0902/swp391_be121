package fu.se.myplatform.repository;

import fu.se.myplatform.entity.BlogLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface BlogLikeRepository extends JpaRepository<BlogLike, Long> {
    boolean existsByBlogIdAndUserId(Long blogId, Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM BlogLike bl WHERE bl.blogId = ?1 AND bl.userId = ?2")
    void deleteByBlogIdAndUserId(Long blogId, Long userId);
}
