package fu.se.myplatform.repository;

import fu.se.myplatform.entity.MutedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface MutedUserRepository extends JpaRepository<MutedUser, Long> {
    Optional<MutedUser> findByUser_UserIdAndMutedUntilAfterOrMutedUntilIsNull(Long userId, LocalDateTime now);
}
