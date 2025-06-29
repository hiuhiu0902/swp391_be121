package fu.se.myplatform.repository;

import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.SmokingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SmokingRecordRepository extends JpaRepository<SmokingRecord, Long> {
    Optional<SmokingRecord> findByAccountAndDate(Account account, LocalDate date);

    List<SmokingRecord> findByAccountAndDateBetweenOrderByDateAsc(
        Account account,
        LocalDate startDate,
        LocalDate endDate
    );

    List<SmokingRecord> findByAccountOrderByDateDesc(Account account);
}
