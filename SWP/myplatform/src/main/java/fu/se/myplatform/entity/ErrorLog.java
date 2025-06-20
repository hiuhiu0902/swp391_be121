package fu.se.myplatform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class ErrorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
    private String stackTrace;
    private LocalDateTime errorTime;

    public ErrorLog() {}
    public ErrorLog(String message, String stackTrace, LocalDateTime errorTime) {
        this.message = message;
        this.stackTrace = stackTrace;
        this.errorTime = errorTime;
    }


}

