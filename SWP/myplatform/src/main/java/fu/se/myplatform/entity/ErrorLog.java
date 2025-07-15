package fu.se.myplatform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "error_logs")
public class ErrorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String message;

    @Column(columnDefinition = "TEXT")
    private String stackTrace;

    @Column(nullable = false)
    private LocalDateTime errorTime;

    @Column(name = "event_type")
    private String eventType;  // Loại sự kiện: ERROR, MUTE, UNMUTE, DELETE, etc.

    @Column(name = "affected_user")
    private String affectedUser;  // User bị ảnh hưởng bởi hành động

    @Column(name = "performed_by")
    private String performedBy;  // User thực hiện hành động

    public ErrorLog() {}

    public ErrorLog(String message, String stackTrace, LocalDateTime errorTime) {
        this.message = message;
        this.stackTrace = stackTrace;
        this.errorTime = errorTime;
    }

    // Constructor mới cho các event có liên quan đến user
    public ErrorLog(String eventType, String message, String affectedUser, String performedBy) {
        this.eventType = eventType;
        this.message = message;
        this.affectedUser = affectedUser;
        this.performedBy = performedBy;
        this.errorTime = LocalDateTime.now();
    }
}
