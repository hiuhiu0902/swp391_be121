package fu.se.myplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {
    @NotBlank(message = "Comment content cannot be blank")
    private String content;

    private Long parentId;        // ID của comment được reply
    private String replyToUser;   // Username của người được reply
    private String mentionedText; // Phần text mention người được reply (VD: @John Doe)
}
