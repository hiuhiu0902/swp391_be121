package fu.se.myplatform.service;

import fu.se.myplatform.dto.SmokingRecordRequest;
import fu.se.myplatform.dto.WeeklyProgressStats;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.QuitPlan;
import fu.se.myplatform.entity.SmokingRecord;
import fu.se.myplatform.repository.QuitPlanRepository;
import fu.se.myplatform.repository.SmokingRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SmokingRecordService {
    @Autowired
    private SmokingRecordRepository smokingRecordRepository;

    @Autowired
    private QuitPlanRepository quitPlanRepository;

    @Autowired
    private AuthenticationService authenticationService;

    public SmokingRecord saveSmokingRecord(SmokingRecordRequest request, LocalDate date) {
        Account account = authenticationService.getCurrentAccount();
        QuitPlan plan = quitPlanRepository.findByAccount(account)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kế hoạch cai thuốc!"));

        // Kiểm tra xem date có nằm trong khoảng thời gian của kế hoạch không
        if (date.isBefore(plan.getStartDate())) {
            throw new RuntimeException("Ngày ghi nhận không thể trước ngày bắt đầu kế hoạch!");
        }

        SmokingRecord record = smokingRecordRepository
                .findByAccountAndDate(account, date)
                .orElse(new SmokingRecord());

        record.setAccount(account);
        record.setDate(date);
        record.setCigarettesSmoked(request.getCigarettesSmoked());

        return smokingRecordRepository.save(record);
    }

    public int getTargetForDate(LocalDate date, QuitPlan plan) {
        LocalDate planStartDate = plan.getStartDate();
        long weeksDiff = java.time.temporal.ChronoUnit.WEEKS.between(planStartDate, date);
        int weekNumber = (int) weeksDiff + 1;

        return plan.getTaperingSchedule().stream()
                .filter(step -> step.getWeekNumber() == weekNumber)
                .findFirst()
                .map(step -> step.getCigarettesPerDay())
                .orElse(plan.getCigarettesPerDay());
    }

    public SmokingRecord getRecordByDate(LocalDate date) {
        Account account = authenticationService.getCurrentAccount();
        return smokingRecordRepository.findByAccountAndDate(account, date).orElse(null);
    }

    public List<SmokingRecord> getRecordsByDateRange(LocalDate startDate, LocalDate endDate) {
        Account account = authenticationService.getCurrentAccount();
        return smokingRecordRepository.findByAccountAndDateBetweenOrderByDateAsc(
            account, startDate, endDate
        );
    }

    public List<SmokingRecord> getPlanWeekRecords(Integer weekNumber) {
        Account account = authenticationService.getCurrentAccount();
        QuitPlan plan = quitPlanRepository.findByAccount(account)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kế hoạch cai thuốc!"));

        LocalDate planStartDate = plan.getStartDate();

        if (weekNumber == null) {
            LocalDate today = LocalDate.now();
            long weeksDiff = java.time.temporal.ChronoUnit.WEEKS.between(planStartDate, today);
            weekNumber = (int) weeksDiff + 1;
        }

        LocalDate weekStartDate = planStartDate.plusWeeks(weekNumber - 1);
        LocalDate weekEndDate = weekStartDate.plusDays(6);

        return getRecordsByDateRange(weekStartDate, weekEndDate);
    }

    public WeeklyProgressStats getWeeklyProgress(int weekNumber) {
        Account account = authenticationService.getCurrentAccount();
        QuitPlan plan = quitPlanRepository.findByAccount(account)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kế hoạch cai thuốc!"));

        LocalDate planStartDate = plan.getStartDate();
        LocalDate weekStartDate = planStartDate.plusWeeks(weekNumber - 1);
        LocalDate weekEndDate = weekStartDate.plusDays(6);

        int targetCigarettesPerDay = plan.getTaperingSchedule().stream()
                .filter(step -> step.getWeekNumber() == weekNumber)
                .findFirst()
                .map(step -> step.getCigarettesPerDay())
                .orElse(0);

        List<SmokingRecord> weeklyRecords = getRecordsByDateRange(weekStartDate, weekEndDate);

        WeeklyProgressStats stats = new WeeklyProgressStats();
        stats.setWeekNumber(weekNumber);
        stats.setWeekStartDate(weekStartDate);
        stats.setWeekEndDate(weekEndDate);
        stats.setTargetCigarettesPerDay(targetCigarettesPerDay);

        int totalSmoked = weeklyRecords.stream()
                .mapToInt(SmokingRecord::getCigarettesSmoked)
                .sum();
        stats.setTotalCigarettesSmoked(totalSmoked);

        int initialDailyCount = plan.getCigarettesPerDay();
        int initialWeeklyTotal = initialDailyCount * 7;
        stats.setCigarettesReduction(initialWeeklyTotal - totalSmoked);

        List<WeeklyProgressStats.DailyProgress> dailyProgress = new ArrayList<>();
        int daysOver = 0;
        int daysOn = 0;
        int daysUnder = 0;

        for (LocalDate date = weekStartDate; !date.isAfter(weekEndDate); date = date.plusDays(1)) {
            final LocalDate currentDate = date;
            Optional<SmokingRecord> recordOpt = weeklyRecords.stream()
                    .filter(r -> r.getDate().equals(currentDate))
                    .findFirst();

            WeeklyProgressStats.DailyProgress daily = new WeeklyProgressStats.DailyProgress();
            daily.setDate(date);
            daily.setTargetCigarettes(targetCigarettesPerDay);

            if (recordOpt.isPresent()) {
                int smoked = recordOpt.get().getCigarettesSmoked();
                daily.setCigarettesSmoked(smoked);

                if (smoked > targetCigarettesPerDay) {
                    daily.setStatus("OVER");
                    daysOver++;
                } else if (smoked == targetCigarettesPerDay) {
                    daily.setStatus("ON_TARGET");
                    daysOn++;
                } else {
                    daily.setStatus("UNDER");
                    daysUnder++;
                }
            } else {
                daily.setCigarettesSmoked(0);
                daily.setStatus("NO_RECORD");
            }

            dailyProgress.add(daily);
        }

        stats.setDaysOverTarget(daysOver);
        stats.setDaysOnTarget(daysOn);
        stats.setDaysUnderTarget(daysUnder);
        stats.setDailyProgress(dailyProgress);

        if (weekNumber > 1) {
            LocalDate previousWeekStart = weekStartDate.minusWeeks(1);
            LocalDate previousWeekEnd = weekStartDate.minusDays(1);
            int previousTotal = getRecordsByDateRange(previousWeekStart, previousWeekEnd)
                    .stream()
                    .mapToInt(SmokingRecord::getCigarettesSmoked)
                    .sum();
            stats.setPreviousWeeklyTotal(previousTotal);
        }

        return stats;
    }
}
