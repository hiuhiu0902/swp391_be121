package fu.se.myplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private Long blogId;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
    private boolean isDeleted;
}
