package fu.se.myplatform.service;

import fu.se.myplatform.entity.LoginLog;
import fu.se.myplatform.entity.ErrorLog;
import fu.se.myplatform.repository.LoginLogRepository;
import fu.se.myplatform.repository.ErrorLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LogEventService {
    @Autowired
    private LoginLogRepository loginLogRepository;
    @Autowired
    private ErrorLogRepository errorLogRepository;

    public void logLogin(String username) {
        LoginLog log = new LoginLog(username, LocalDateTime.now(), false);
        loginLogRepository.save(log);
    }

    public void logRegister(String username) {
        LoginLog log = new LoginLog(username, LocalDateTime.now(), true);
        loginLogRepository.save(log);
    }

    public void logError(String message, String stackTrace) {
        ErrorLog log = new ErrorLog(message, stackTrace, LocalDateTime.now());
        errorLogRepository.save(log);
    }
}

