package fu.se.myplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MutedUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Account user;  // Người bị mute

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "muted_by")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Account mutedBy;  // Staff người thực hiện mute

    @Column()
    private LocalDateTime mutedAt;

    private LocalDateTime mutedUntil;  // Null nghĩa là mute vĩnh viễn

    @Column(columnDefinition = "TEXT")
    private String reason;

    @PrePersist
    protected void onCreate() {
        mutedAt = LocalDateTime.now();
    }
}
