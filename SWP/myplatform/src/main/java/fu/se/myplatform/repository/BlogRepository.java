package fu.se.myplatform.repository;

import fu.se.myplatform.entity.Blog;
import fu.se.myplatform.enums.BlogCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

    @Query("SELECT b FROM Blog b WHERE " +
           "(:search IS NULL OR b.title LIKE %:search% OR " +
           "b.content LIKE %:search%) AND " +
           "(:category IS NULL OR b.category = :category) AND " +
           "(:featured IS NULL OR b.isFeatured = :featured) AND " +
           "b.isPublished = true " +
           "ORDER BY b.createdAt DESC")
    List<Blog> findBySearchCriteriaAndFeatured(String search, BlogCategory category, Boolean featured);

    @Query("SELECT b FROM Blog b WHERE " +
           "b.userId = :userId AND " +
           "(:search IS NULL OR b.title LIKE %:search% OR " +
           "b.content LIKE %:search%) AND " +
           "(:category IS NULL OR b.category = :category) " +
           "ORDER BY b.createdAt DESC")
    List<Blog> findByUserIdAndSearchCriteria(Long userId, String search, BlogCategory category);

    @Query(value = "WITH BlogStats AS (" +
           "    SELECT blog_id, COUNT(*) as comment_count " +
           "    FROM comment " +
           "    GROUP BY blog_id" +
           ")" +
           "SELECT TOP(:limit) b.id, b.created_at, b.updated_at, b.category, b.is_featured, " +
           "b.is_published, b.likes, b.user_id, b.view_count, " +
           "b.title, b.content, b.thumbnail, b.allow_comments, " +
           "(b.view_count + b.likes * 2 + ISNULL(bs.comment_count, 0) * 4) as engagement_score " +
           "FROM blog b " +
           "LEFT JOIN BlogStats bs ON b.id = bs.blog_id " +
           "WHERE b.is_published = 1 " +
           "AND (:category IS NULL OR b.category = :category) " +
           "AND (:featured IS NULL OR b.is_featured = :featured) " +
           "ORDER BY engagement_score DESC, b.created_at DESC", nativeQuery = true)
    List<Blog> findBlogFeed(@Param("category") String category, @Param("limit") int limit, @Param("featured") Boolean featured);

    /**
     * Đếm số blog theo category
     */
    long countByCategory(BlogCategory category);

    @Query("SELECT COUNT(b) FROM Blog b WHERE b.category = :category AND b.isPublished = true")
    long countByCategoryAndPublishedTrue(@Param("category") BlogCategory category);

    @Query("SELECT COUNT(b) FROM Blog b WHERE b.category = :category AND b.isFeatured = true")
    long countByCategoryAndFeaturedTrue(@Param("category") BlogCategory category);

    @Query("SELECT b FROM Blog b WHERE b.category = :category AND b.isFeatured = true")
    List<Blog> findByCategoryAndFeaturedTrue(@Param("category") BlogCategory category, Pageable pageable);

    @Query("SELECT b FROM Blog b WHERE " +
           "(:search IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:category IS NULL OR b.category = :category)")
    List<Blog> searchBlogs(@Param("search") String search, @Param("category") BlogCategory category);

    @Modifying
    @Query(value = "ALTER TABLE blog ADD likes bigint DEFAULT 0 NOT NULL", nativeQuery = true)
    @Transactional
    void addLikesColumn();

    @Modifying
    @Query(value = "ALTER TABLE blog ADD view_count bigint DEFAULT 0 NOT NULL", nativeQuery = true)
    @Transactional
    void addViewCountColumn();

    @Query("SELECT b FROM Blog b " +
           "LEFT JOIN b.comments c " +
           "WHERE b.isPublished = true " +
           "AND (:category IS NULL OR b.category = :category) " +
           "GROUP BY b " +
           "ORDER BY (b.viewCount + b.likes * 2 + SIZE(b.comments) * 4) DESC")
    List<Blog> findMostEngagedBlogs(@Param("category") BlogCategory category, Pageable pageable);

    @Modifying
    @Query("UPDATE Blog b SET b.viewCount = b.viewCount + 1 WHERE b.id = :blogId")
    @Transactional
    void incrementViewCount(@Param("blogId") Long blogId);

    @Modifying
    @Query("UPDATE Blog b SET b.likes = b.likes + 1 WHERE b.id = :blogId")
    @Transactional
    void incrementLikes(@Param("blogId") Long blogId);

    @Modifying
    @Query("UPDATE Blog b SET b.likes = b.likes - 1 WHERE b.id = :blogId AND b.likes > 0")
    @Transactional
    void decrementLikes(@Param("blogId") Long blogId);

    @Query(value = "WITH BlogStats AS (" +
           "    SELECT blog_id, COUNT(*) as comment_count " +
           "    FROM comment " +
           "    GROUP BY blog_id" +
           ")" +
           "SELECT b.*, (b.view_count + b.likes * 2 + ISNULL(bs.comment_count, 0) * 4) as engagement_score " +
           "FROM blog b " +
           "LEFT JOIN BlogStats bs ON b.id = bs.blog_id " +
           "WHERE b.is_published = 1 " +
           "AND (:category IS NULL OR b.category = :category) " +
           "AND (:featured IS NULL OR b.is_featured = :featured) " +
           "ORDER BY engagement_score DESC, b.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM blog b " +
           "WHERE b.is_published = 1 " +
           "AND (:category IS NULL OR b.category = :category) " +
           "AND (:featured IS NULL OR b.is_featured = :featured)",
           nativeQuery = true)
    Page<Blog> findBlogFeedPaged(
            @Param("category") String category,
            @Param("featured") Boolean featured,
            Pageable pageable);
}
