package fu.se.myplatform.service;

import fu.se.myplatform.dto.MuteRequest;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.MutedUser;
import fu.se.myplatform.exception.BadRequestException;
import fu.se.myplatform.exception.NotFoundException;
import fu.se.myplatform.repository.AccountRepository;
import fu.se.myplatform.repository.MutedUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ModerationService {
    private final MutedUserRepository mutedUserRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final LogEventService logEventService;

    @Transactional
    public void muteUser(Long userId, MuteRequest request) {
        Account userToMute = accountRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        Account staff = accountService.getCurrentUser();

        // Kiểm tra xem người dùng đã bị mute chưa
        mutedUserRepository.findByUser_UserIdAndMutedUntilAfterOrMutedUntilIsNull(userId, LocalDateTime.now())
                .ifPresent(existing -> {
                    throw new BadRequestException("Người dùng này đã bị mute");
                });

        MutedUser mutedUser = new MutedUser();
        mutedUser.setUser(userToMute);
        mutedUser.setMutedBy(staff);
        mutedUser.setReason(request.getReason());
        mutedUser.setMutedUntil(request.getMutedUntil());

        try {
            mutedUserRepository.save(mutedUser);
            logEventService.logUserMuted(userToMute.getUsername(), staff.getUsername(), request.getReason());
        } catch (Exception e) {
            logEventService.logError("Lỗi khi mute người dùng", e.getMessage());
            throw new RuntimeException("Không thể mute người dùng: " + e.getMessage());
        }
    }

    @Transactional
    public void unmuteUser(Long userId) {
        Account userToUnmute = accountRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        Account staff = accountService.getCurrentUser();

        mutedUserRepository.findByUser_UserIdAndMutedUntilAfterOrMutedUntilIsNull(userId, LocalDateTime.now())
                .ifPresentOrElse(mutedUser -> {
                    mutedUser.setMutedUntil(LocalDateTime.now());
                    mutedUserRepository.save(mutedUser);
                    logEventService.logUserUnmuted(userToUnmute.getUsername(), staff.getUsername());
                }, () -> {
                    throw new BadRequestException("Người dùng này không bị mute");
                });
    }

    public boolean isUserMuted(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return mutedUserRepository.findByUser_UserIdAndMutedUntilAfterOrMutedUntilIsNull(userId, now)
                .isPresent();
    }
}
