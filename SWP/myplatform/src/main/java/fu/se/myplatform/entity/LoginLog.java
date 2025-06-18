package fu.se.myplatform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
    public boolean isRegister() { return isRegister; }
    public void setRegister(boolean register) { isRegister = register; }
}

