package fu.se.myplatform.service;

import fu.se.myplatform.entity.LoginLog;
import fu.se.myplatform.entity.ErrorLog;
import fu.se.myplatform.repository.LoginLogRepository;
import fu.se.myplatform.repository.ErrorLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public void logUserMuted(String mutedUsername, String staffUsername, String reason) {
        ErrorLog log = new ErrorLog(
            "MUTE",
            String.format("User %s was muted by %s. Reason: %s", mutedUsername, staffUsername, reason),
            mutedUsername,
            staffUsername
        );
        errorLogRepository.save(log);
    }

    public void logUserUnmuted(String unmutedUsername, String staffUsername) {
        ErrorLog log = new ErrorLog(
            "UNMUTE",
            String.format("User %s was unmuted by %s", unmutedUsername, staffUsername),
            unmutedUsername,
            staffUsername
        );
        errorLogRepository.save(log);
    }

    public void logAccountDeletion(String username) {
        ErrorLog log = new ErrorLog(
            "DELETE_ACCOUNT",
            String.format("Account %s was deleted", username),
            username,
            getCurrentUsername()
        );
        errorLogRepository.save(log);
    }

    public void logError(String message, String stackTrace) {
        ErrorLog log = new ErrorLog();
        log.setEventType("ERROR");
        log.setMessage(message);
        log.setStackTrace(stackTrace);
        log.setErrorTime(LocalDateTime.now());
        log.setAffectedUser(getCurrentUsername());
        errorLogRepository.save(log);
    }

    public void logActionFailed(String action, String username, String error) {
        ErrorLog log = new ErrorLog();
        log.setEventType("ACTION_FAILED");
        log.setMessage(String.format("Action %s failed for user %s", action, username));
        log.setStackTrace(error);
        log.setErrorTime(LocalDateTime.now());
        log.setAffectedUser(username);
        log.setPerformedBy(getCurrentUsername());
        errorLogRepository.save(log);
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "SYSTEM";
        }
    }
}
