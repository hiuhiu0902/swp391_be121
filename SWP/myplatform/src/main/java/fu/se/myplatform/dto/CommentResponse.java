package fu.se.myplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    @JsonProperty("isDeleted")
    private boolean deleted;

    // Thông tin về người comment
    private String userFullName;

    // Thông tin về reply
    private Long parentId;
    private String replyToUserFullName;
    private String replyToUserName;
    private String mentionedText;
    private List<CommentResponse> replies = new ArrayList<>();

    private int countReplies;
    private boolean hasReplies;
    private boolean reply;
}
