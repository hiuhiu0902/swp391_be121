package fu.se.myplatform.service;

import fu.se.myplatform.repository.LoginLogRepository;
import fu.se.myplatform.repository.ErrorLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LogReportService {
    @Autowired
    private LoginLogRepository loginLogRepository;
    @Autowired
    private ErrorLogRepository errorLogRepository;

    public long getLoginCount() {
        return loginLogRepository.countByIsRegister(false);
    }

    public long getRegisterCount() {
        return loginLogRepository.countByIsRegister(true);
    }

    public long getErrorCount() {
        return errorLogRepository.count();
    }

    /**
     * Lấy số liệu thống kê đăng nhập theo ngày trong khoảng thời gian
     */
    public Map<String, Long> getLoginStatsByDay(int lastNDays) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(lastNDays);
        List<Object[]> stats = loginLogRepository.countByDateAndIsRegister(startDate, false);

        Map<String, Long> result = new HashMap<>();
        for (Object[] stat : stats) {
            java.sql.Date date = (java.sql.Date) stat[0];
            Long count = (Long) stat[1];
            result.put(date.toString(), count);
        }
        return result;
    }

    /**
     * Lấy số liệu thống kê đăng ký theo ngày trong khoảng thời gian
     */
    public Map<String, Long> getRegistrationStatsByDay(int lastNDays) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(lastNDays);
        List<Object[]> stats = loginLogRepository.countByDateAndIsRegister(startDate, true);

        Map<String, Long> result = new HashMap<>();
        for (Object[] stat : stats) {
            java.sql.Date date = (java.sql.Date) stat[0];
            Long count = (Long) stat[1];
            result.put(date.toString(), count);
        }
        return result;
    }

    /**
     * Lấy số liệu thống kê đăng ký theo tháng
     */
    public Map<String, Long> getRegistrationStatsByMonth(int lastNMonths) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(lastNMonths);
        List<Object[]> stats = loginLogRepository.countByMonthAndIsRegister(startDate, true);

        Map<String, Long> result = new HashMap<>();
        for (Object[] stat : stats) {
            Integer month = (Integer) stat[0];
            Integer year = (Integer) stat[1];
            Long count = (Long) stat[2];
            result.put(year + "-" + String.format("%02d", month), count);
        }
        return result;
    }

    public long getLoginCountByDays(int days) {
        return loginLogRepository.countByLoginTimeAfterAndIsRegister(
            java.time.LocalDateTime.now().minusDays(days),
            false
        );
    }

    public long getRegisterCountByDays(int days) {
        return loginLogRepository.countByLoginTimeAfterAndIsRegister(
            java.time.LocalDateTime.now().minusDays(days),
            true
        );
    }
}
