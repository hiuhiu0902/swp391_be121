package fu.se.myplatform.repository;

import fu.se.myplatform.entity.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {
    long count();
}

