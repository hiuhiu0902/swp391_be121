package fu.se.myplatform.repository;

import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.QuitPlan;
import fu.se.myplatform.enums.QuitPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface QuitPlanRepository extends JpaRepository<QuitPlan, Long> {
    Optional<QuitPlan> findByAccount(Account account);

    /**
     * Đếm tất cả quit plans
     */
    @Query(value = "SELECT COUNT(*) FROM quit_plans", nativeQuery = true)
    long countAll();

    long countByStatus(QuitPlanStatus status);
}
