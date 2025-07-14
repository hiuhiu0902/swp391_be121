package fu.se.myplatform.repository;

import fu.se.myplatform.entity.Blog;
import fu.se.myplatform.enums.BlogCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

    @Query("SELECT b FROM Blog b WHERE " +
           "(:search IS NULL OR (LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.content) LIKE LOWER(CONCAT('%', :search, '%')))) AND " +
           "(:category IS NULL OR b.category = :category) AND " +
           "(:featured IS NULL OR b.isFeatured = :featured) AND " +
           "b.isPublished = true")
    Page<Blog> findBySearchCriteriaAndFeatured(String search, BlogCategory category, Boolean featured, Pageable pageable);

    @Query("SELECT b FROM Blog b WHERE " +
           "b.userId = :userId AND " +
           "(:search IS NULL OR (LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.content) LIKE LOWER(CONCAT('%', :search, '%')))) AND " +
           "(:category IS NULL OR b.category = :category)")
    Page<Blog> findByUserIdAndSearchCriteria(Long userId, String search, BlogCategory category, Pageable pageable);

    @Query(value = "SELECT b FROM Blog b WHERE " +
           "(:lastId IS NULL OR b.id < :lastId) AND " +
           "(:category IS NULL OR b.category = :category) AND " +
           "(:featured IS NULL OR b.isFeatured = :featured) AND " +
           "b.isPublished = true " +
           "ORDER BY b.id DESC")
    Page<Blog> findBlogFeed(Long lastId, BlogCategory category, Boolean featured, Pageable pageable);

    /**
     * Đếm số blog theo category
     */
    long countByCategory(BlogCategory category);
}
