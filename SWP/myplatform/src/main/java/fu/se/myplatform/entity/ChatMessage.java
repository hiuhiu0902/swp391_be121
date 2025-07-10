package fu.se.myplatform.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long senderId;     // ID người gửi (VIPMember hoặc Coach)
    private Long receiverId;   // ID người nhận
    private String content;    // Nội dung tin nhắn
    private LocalDateTime timestamp;
}
