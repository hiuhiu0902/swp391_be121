package fu.se.myplatform.repository;

import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.SmokingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SmokingReportRepository extends JpaRepository<SmokingRecord, Long> {
    // Custom query methods can be defined here if needed
    Optional<SmokingRecord> findByAccountAndDate(Account account, LocalDate date);

    List<SmokingRecord> findByAccountAndDateBetween(Account account, LocalDate dateAfter, LocalDate dateBefore);
}
