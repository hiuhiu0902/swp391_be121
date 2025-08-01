package fu.se.myplatform.dto;

import fu.se.myplatform.enums.BlogCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogResponse {
    private Long id;
    private String title;
    private String content;
    private String thumbnail;
    private String image;  // URL của ảnh blog
    private Long userId;
    private String userName;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BlogCategory category;
    private boolean isPublished;
    private boolean isFeatured;
    private Long likes = 0L;  // Số lượng like
    private boolean isLiked;  // Trạng thái like của user hiện tại
}
