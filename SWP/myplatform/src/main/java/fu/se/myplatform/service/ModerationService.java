package fu.se.myplatform.service;

import fu.se.myplatform.dto.MuteRequest;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.MutedUser;
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

    @Transactional
    public void muteUser(Long userId, MuteRequest request) {
        Account userToMute = accountRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Account staff = accountService.getCurrentUser();

        MutedUser mutedUser = new MutedUser();
        mutedUser.setUser(userToMute);
        mutedUser.setMutedBy(staff);
        mutedUser.setReason(request.getReason());
        mutedUser.setMutedUntil(request.getMutedUntil());

        mutedUserRepository.save(mutedUser);
    }

    @Transactional
    public void unmuteUser(Long userId) {
        Account userToUnmute = accountRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        mutedUserRepository.findByUser_UserIdAndMutedUntilAfterOrMutedUntilIsNull(userId, LocalDateTime.now())
                .ifPresent(mutedUser -> {
                    mutedUser.setMutedUntil(LocalDateTime.now());
                    mutedUserRepository.save(mutedUser);
                });
    }

    public boolean isUserMuted(Long userId) {
        return mutedUserRepository.findByUser_UserIdAndMutedUntilAfterOrMutedUntilIsNull(
                userId, LocalDateTime.now()).isPresent();
    }
}
