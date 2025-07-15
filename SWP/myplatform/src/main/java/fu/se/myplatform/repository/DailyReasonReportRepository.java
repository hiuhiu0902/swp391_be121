package fu.se.myplatform.repository;

import fu.se.myplatform.entity.DailyReasonReport;
import fu.se.myplatform.entity.QuitPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyReasonReportRepository extends JpaRepository<DailyReasonReport, Long> {
    List<DailyReasonReport> findAllByQuitPlan(QuitPlan quitPlan);
    void deleteAllByQuitPlan(QuitPlan quitPlan);
}
