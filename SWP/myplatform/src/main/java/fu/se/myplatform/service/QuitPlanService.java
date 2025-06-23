package fu.se.myplatform.service;

import fu.se.myplatform.dto.QuitPlanRequest;
import fu.se.myplatform.dto.QuitPlanResponse;
import fu.se.myplatform.dto.TaperingStep;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.QuitPlan;
import fu.se.myplatform.enums.QuitReason;
import fu.se.myplatform.enums.Triggers;
import fu.se.myplatform.exception.MyException;
import fu.se.myplatform.repository.QuitPlanRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        // Nếu đã có kế hoạch thì trả về lỗi, không xóa tự động
        if (quitPlanRepository.findByAccount(account).isPresent()) {
            throw new MyException("Bạn đã có kế hoạch cai thuốc. Vui lòng xóa kế hoạch cũ trước khi tạo mới!");
        }
        // Validate số tuần chỉ được phép là 2,3,4,5,6
        int weeks = planRequest.getDurationWeeks();
        if (weeks < 2 || weeks > 6) {
            throw new MyException("Thời gian kế hoạch chỉ được phép từ 2 đến 6 tuần!");
        }
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
                .divide(BigDecimal.valueOf(20), RoundingMode.HALF_UP); // Assuming 20 cigarettes per pack
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
            TaperingStep step = new TaperingStep(i, weekStart, weekEnd, target, null);
            steps.add(step);
            current -= decreasePerWeek;

        // Thêm 1 bước thông báo "Từ ngày ... bạn sẽ bỏ thuốc" dựa trên LocalDate và số thứ tự ngày thực tế
        java.time.LocalDate quitDate = startDate.plusDays(durationWeeks * 7);
        long dayNumber = java.time.temporal.ChronoUnit.DAYS.between(startDate, quitDate) + 1;
        TaperingStep quitStep = new TaperingStep(durationWeeks + 1, quitDate, quitDate, 0, "Từ ngày " + quitDate + " (ngày thứ " + dayNumber + ") bạn sẽ bỏ thuốc");
        steps.add(quitStep);
        if (i == durationWeeks) target = 0;
        TaperingStep step2 = new TaperingStep(i, weekStart, weekEnd, target, null);
        steps.add(step);
        current -= decreasePerWeek;
    }
        return steps;
}

    private List<String> suggestTips(Set<Triggers> triggers) {
        List<String> tips = new java.util.ArrayList<>();
        for (Triggers trigger : triggers) {
            switch (trigger) {
                // Social
                case OFFERED_CIGARETTE:
                    tips.add("Luyện tập nói 'Không, cảm ơn' khi được mời thuốc.");
                    break;
                case DRINKING_ALCOHOL:
                    tips.add("Tránh hoặc hạn chế uống rượu bia, vì có thể khiến bạn muốn hút thuốc trở lại.");
                    break;
                case PARTY_OR_SOCIAL_EVENT:
                    tips.add("Tránh tiệc tùng, đông người trong giai đoạn đầu. Đi cùng người ủng hộ bạn cai thuốc.");
                    break;
                case AROUND_OTHERS_SMOKING:
                    tips.add("Hạn chế ở gần những người đang hút thuốc. Nếu cần, hãy ra chỗ khác.");
                    break;
                case SEEING_SOMEONE_SMOKE:
                    tips.add("Chủ động nhìn đi chỗ khác hoặc tập trung vào việc khác khi thấy người khác hút thuốc.");
                    break;
                case SMELLING_CIGARETTE_SMOKE:
                    tips.add("Tránh những nơi có mùi thuốc lá. Giữ môi trường sống sạch sẽ, thơm mát.");
                    break;
                // Withdrawal
                case IRRITABLE:
                    tips.add("Thở sâu, nghe nhạc thư giãn hoặc tập thể dục nhẹ khi cảm thấy cáu gắt.");
                    break;
                case RESTLESS_OR_JUMPY:
                    tips.add("Đi bộ hoặc vận động nhẹ giúp giảm cảm giác bồn chồn.");
                    break;
                case STRONG_CRAVINGS:
                    tips.add("Khi thèm thuốc, hãy nhai kẹo cao su hoặc uống nước, chuyển sự chú ý sang việc khác.");
                    break;
                case HARD_TIME_CONCENTRATING:
                    tips.add("Chia nhỏ công việc, nghỉ giải lao thường xuyên, tập trung vào hít thở.");
                    break;
                case WAKING_UP:
                    tips.add("Khi vừa thức dậy, hãy đánh răng hoặc uống một cốc nước để tránh nghĩ đến thuốc.");
                    break;

                // Routine
                case ON_MY_PHONE:
                    tips.add("Khi dùng điện thoại, hãy bận rộn tay bằng đồ vật khác như bút hoặc bóng bóp.");
                    break;
                case DOWN_TIME:
                    tips.add("Chuẩn bị trước các hoạt động thay thế như đọc sách, nghe nhạc, tập thể dục khi rảnh.");
                    break;
                case DRINKING_COFFEE:
                    tips.add("Hạn chế uống cà phê, có thể thay bằng trà, nước ép hoặc nước lọc.");
                    break;
                case FINISHING_A_MEAL:
                    tips.add("Sau khi ăn, đánh răng, nhai kẹo hoặc đi dạo thay vì hút thuốc.");
                    break;
                case CIGARETTES_ON_TV:
                    tips.add("Chuyển kênh hoặc tránh xem những cảnh có người hút thuốc.");
                    break;
                case WAITING_FOR_RIDE:
                    tips.add("Mang theo sách, nghe nhạc hoặc chơi trò chơi điện thoại khi chờ đợi.");
                    break;
                case WALKING_OR_DRIVING:
                    tips.add("Giữ tay bận rộn bằng đồ vật nhỏ, hoặc bật nhạc khi lái xe/đi bộ.");
                    break;
                case WATCHING_TV_OR_GAMES:
                    tips.add("Nhâm nhi trái cây, kẹo cao su khi xem TV hoặc chơi game.");
                    break;
                case WORKING_OR_STUDYING:
                    tips.add("Lập kế hoạch làm việc, nghỉ ngắn xen kẽ để tránh nghĩ đến thuốc.");
                    break;

                // Emotional
                case ANGRY:
                    tips.add("Khi tức giận, hãy hít thở sâu hoặc nói chuyện với ai đó bạn tin tưởng.");
                    break;
                case ANXIOUS:
                    tips.add("Tập thiền, hít thở sâu hoặc nghe nhạc nhẹ để giảm lo lắng.");
                    break;
                case BORED:
                    tips.add("Lên danh sách các hoạt động nhỏ để làm khi buồn chán, ví dụ như vẽ, đọc báo, tập thể dục.");
                    break;
                case FRUSTRATED:
                    tips.add("Viết nhật ký hoặc chia sẻ với bạn bè về điều khiến bạn bực mình.");
                    break;
                case HAPPY:
                    tips.add("Ăn mừng thành công bằng cách lành mạnh, không cần đến thuốc lá.");
                    break;
                case LONELY:
                    tips.add("Liên lạc với bạn bè hoặc tham gia nhóm hỗ trợ khi cảm thấy cô đơn.");
                    break;
                case SAD:
                    tips.add("Tham gia hoạt động ngoài trời, chia sẻ cảm xúc với người thân.");
                    break;
                case STRESSED:
                    tips.add("Thử các bài tập thở, yoga hoặc hoạt động thể chất để giải tỏa stress.");
                    break;

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

public void deleteCurrentUserPlan() {
    Account account = authenticationService.getCurrentAccount();
    QuitPlan plan = quitPlanRepository.findByAccount(account)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy kế hoạch để xóa!"));
    quitPlanRepository.delete(plan);
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
