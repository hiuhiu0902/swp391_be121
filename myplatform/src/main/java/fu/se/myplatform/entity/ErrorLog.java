package fu.se.myplatform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ErrorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
    private String stackTrace;
    private LocalDateTime errorTime;

    // Constructors
    public ErrorLog() {}
    public ErrorLog(String message, String stackTrace, LocalDateTime errorTime) {
        this.message = message;
        this.stackTrace = stackTrace;
        this.errorTime = errorTime;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStackTrace() { return stackTrace; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
    public LocalDateTime getErrorTime() { return errorTime; }
    public void setErrorTime(LocalDateTime errorTime) { this.errorTime = errorTime; }
}

