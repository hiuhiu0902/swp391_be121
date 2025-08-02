package fu.se.myplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import fu.se.myplatform.enums.BlogCategory;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Blog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String title;

//    @Column(columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String content;

    private String thumbnail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Account user;  // Liên kết với Account để xác định người đăng

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;   // Để dễ dàng truy vấn mà không cần join
    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlogCategory category;

    private boolean isPublished = false;

    private boolean isFeatured = false;  // Để đánh dấu bài viết nổi bật

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long viewCount = 0L;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long likes = 0L;

    @Column(nullable = false)
    private boolean allowComments = true;

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

//    @Column(columnDefinition = "VARCHAR(MAX)")
    private String image;  // URL của ảnh blog trên Cloudinary

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
