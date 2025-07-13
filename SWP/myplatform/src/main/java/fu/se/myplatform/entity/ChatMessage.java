package fu.se.myplatform.entity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;
}
