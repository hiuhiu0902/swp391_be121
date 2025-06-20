package fu.se.myplatform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Setter
@Getter
@Entity
public class LoginLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private LocalDateTime loginTime;
    private boolean isRegister;

    // Constructors
    public LoginLog() {}
    public LoginLog(String username, LocalDateTime loginTime, boolean isRegister) {
        this.username = username;
        this.loginTime = loginTime;
        this.isRegister = isRegister;
    }


}

