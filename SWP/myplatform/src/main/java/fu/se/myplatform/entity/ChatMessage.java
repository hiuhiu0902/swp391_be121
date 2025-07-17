package fu.se.myplatform.entity;

import fu.se.myplatform.enums.MessageStatus;
import jakarta.persistence.*;
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
    @Column(nullable = false, columnDefinition = "TEXT")// ID người nhận
    private String content;    // Nội dung tin nhắn
    @Column(nullable = false)
    private LocalDateTime sentAt;
    @Column(nullable = false)
    private boolean senderIsCoach;
    // Tham chiếu mối quan hệ (chỉ chat giữa member và coach đã gán với nhau)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // Có thể cần thêm trường `status` để theo dõi trạng thái của tin nhắn
    @Column(nullable = false)
    @Enumerated(EnumType.STRING) // Giả sử bạn có một Enum cho trạng thái
    private MessageStatus status; // Trạng thái tin nhắn (VD: đã đọc, chưa đọc)


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;

    // Có thể thêm trường `editedAt` nếu muốn theo dõi thời gian chỉnh sửa tin nhắn
    @Column
    private LocalDateTime editedAt; // Thời gian chỉnh sửa nếu có


}
