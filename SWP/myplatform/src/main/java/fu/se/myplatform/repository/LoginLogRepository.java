package fu.se.myplatform.repository;

import fu.se.myplatform.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
    long countByIsRegister(boolean isRegister);
}

