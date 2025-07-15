package fu.se.myplatform.repository;

import fu.se.myplatform.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
    long countByIsRegister(boolean isRegister);

    /**
     * Đếm số lượng login/register sau một thời điểm
     */
    long countByLoginTimeAfterAndIsRegister(java.time.LocalDateTime startTime, boolean isRegister);

    /**
     * Thống kê số lượng login/register theo ngày
     */
    @org.springframework.data.jpa.repository.Query("SELECT CAST(l.loginTime AS DATE) as date, COUNT(l) as count FROM LoginLog l " +
            "WHERE l.loginTime >= :startDate AND l.isRegister = :isRegister " +
            "GROUP BY CAST(l.loginTime AS DATE) ORDER BY date")
    List<Object[]> countByDateAndIsRegister(java.time.LocalDateTime startDate, boolean isRegister);

    /**
     * Thống kê số lượng login/register theo tháng
     */
    @org.springframework.data.jpa.repository.Query("SELECT MONTH(l.loginTime) as month, YEAR(l.loginTime) as year, COUNT(l) as count FROM LoginLog l " +
            "WHERE l.loginTime >= :startDate AND l.isRegister = :isRegister " +
            "GROUP BY YEAR(l.loginTime), MONTH(l.loginTime) ORDER BY year, month")
    List<Object[]> countByMonthAndIsRegister(java.time.LocalDateTime startDate, boolean isRegister);
}
