package fu.se.myplatform.service;

import fu.se.myplatform.dto.QuitPlanRequest;
import fu.se.myplatform.dto.QuitPlanResponse;
import fu.se.myplatform.dto.TaperingStep;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.QuitPlan;
import fu.se.myplatform.enums.QuitReason;
import fu.se.myplatform.enums.Triggers;
import fu.se.myplatform.repository.QuitPlanRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuitPlanService {
    @Autowired
    QuitPlanRepository quitPlanRepository;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    ModelMapper modelMapper;

    public QuitPlanResponse createPlan(QuitPlanRequest planRequest) {
        Account account = authenticationService.getCurrentAccount();
        Set<QuitReason> reasons = new HashSet<>(planRequest.getReasons());
        Set<Triggers> triggers = new HashSet<>(planRequest.getTriggers());

        //entity
        QuitPlan plan = new QuitPlan();
        plan.setStartDate(planRequest.getStartDate());
        plan.setCigarettesPerDay(planRequest.getNumberOfCigarettes());
        plan.setReasons(reasons);
        plan.setTriggers(triggers);
        plan.setAccount(account);

        BigDecimal dailyCost = planRequest.getPricePerPack()
                .multiply(BigDecimal.valueOf(planRequest.getNumberOfCigarettes()))
                .divide(BigDecimal.valueOf(20),BigDecimal.ROUND_HALF_UP); // Assuming 20 cigarettes per pack
        plan.setDailyCost(dailyCost);
        plan.setWeeklyCost(dailyCost.multiply(BigDecimal.valueOf(7)));
        plan.setMonthlyCost(dailyCost.multiply(BigDecimal.valueOf(30)));
        plan.setYearlyCost(dailyCost.multiply(BigDecimal.valueOf(365)));

        List<TaperingStep> taperingSchedule = generateTaperingSchedule(
                planRequest.getStartDate(),
                planRequest.getDurationWeeks(),
                planRequest.getNumberOfCigarettes()
        );
        plan.setTaperingSchedule(taperingSchedule);
        quitPlanRepository.save(plan);

        //response
        QuitPlanResponse QuitPlanResponse = modelMapper.map(plan, QuitPlanResponse.class);
        QuitPlanResponse.setTaperingSchedule(taperingSchedule);
        QuitPlanResponse.setTips(suggestTips(triggers));
        QuitPlanResponse.setStartDate(planRequest.getStartDate());
        QuitPlanResponse.setDailyCost(plan.getDailyCost());
        QuitPlanResponse.setWeeklyCost(plan.getWeeklyCost());
        QuitPlanResponse.setMonthlyCost(plan.getMonthlyCost());
        QuitPlanResponse.setYearlyCost(plan.getYearlyCost());

        return QuitPlanResponse;
    }

    private List<TaperingStep> generateTaperingSchedule(
            java.time.LocalDate startDate,
            int durationWeeks,
            int startCigarettesPerDay
    ) {
        List<TaperingStep> steps = new ArrayList<>();
        double decreasePerWeek = (double) startCigarettesPerDay / durationWeeks;
        double current = startCigarettesPerDay;

        for (int i = 1; i <= durationWeeks; i++) {
            int target = (int) Math.round(Math.max(current, 0));
            java.time.LocalDate weekStart = startDate.plusWeeks(i - 1);
            java.time.LocalDate weekEnd = startDate.plusWeeks(i).minusDays(1);
            if (i == durationWeeks) target = 0;
            TaperingStep step = new TaperingStep(i, weekStart, weekEnd, target);
            steps.add(step);
            current -= decreasePerWeek;
        }
        return steps;
    }

    private List<String> suggestTips(Set<Triggers> triggers) {
        List<String> tips = new java.util.ArrayList<>();
        for (Triggers trigger : triggers) {
            switch (trigger) {
                case PARTY_OR_SOCIAL_EVENT:
                    tips.add("Tránh tiệc tùng, đông người trong giai đoạn đầu.");
                    break;
                case DRINKING_COFFEE:
                    tips.add("Hạn chế uống cà phê, thay bằng nước ép, trà.");
                    break;
                case STRONG_CRAVINGS:
                    tips.add("Khi thèm thuốc, hãy nhai kẹo cao su hoặc uống nước.");
                    break;
                // ... thêm các trigger khác
                default:
                    tips.add("Giữ tinh thần thoải mái, tự tin vượt qua các tình huống khó.");
            }
        }
        return tips;
    }

    public QuitPlanResponse getCurrentUserPlan() {
        Account account = authenticationService.getCurrentAccount();
        QuitPlan plan = quitPlanRepository.findByAccount(account)
                .orElseThrow(() -> new RuntimeException("No quit plan found for user"));
        return modelMapper.map(plan, QuitPlanResponse.class);
    }

    public void deletePlan(long planId) {
        Account account = authenticationService.getCurrentAccount();
        QuitPlan plan = quitPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Quit plan not found"));
        if (plan.getAccount().getUserId() != account.getUserId()) {
            throw new RuntimeException("Bạn không có quyền xóa kế hoạch này!");
        }
        quitPlanRepository.deleteById(planId);
    }
}
