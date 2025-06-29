package fu.se.myplatform.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class WeeklyProgressStats {
    private int weekNumber;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private int targetCigarettesPerDay;  // Mục tiêu số điếu/ngày của tuần
    private int totalCigarettesSmoked;   // Tổng số điếu đã hút trong tuần
    private int previousWeeklyTotal;      // Số điếu hút tuần trước
    private int daysOverTarget;           // Số ngày vượt mục tiêu
    private int daysOnTarget;             // Số ngày đạt mục tiêu
    private int daysUnderTarget;          // Số ngày dưới mục tiêu
    private int cigarettesReduction;      // Số điếu giảm so với trạng thái ban đầu
    private List<DailyProgress> dailyProgress; // Chi tiết từng ngày

    @Data
    public static class DailyProgress {
        private LocalDate date;
        private int cigarettesSmoked;
        private int targetCigarettes;
        private String status; // "OVER", "ON_TARGET", "UNDER", "NO_RECORD"
    }
}
