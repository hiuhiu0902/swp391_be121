package fu.se.myplatform.service;

import fu.se.myplatform.dto.*;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.Assessment;
import fu.se.myplatform.entity.QuitPlan;
import fu.se.myplatform.enums.*;
import fu.se.myplatform.exception.BadRequestException;
import fu.se.myplatform.exception.MyException;
import fu.se.myplatform.exception.exception.ResourceNotFoundException;
import fu.se.myplatform.repository.AssessmentRepository;
import fu.se.myplatform.repository.QuitPlanRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
    @Autowired
    FagerstromService fagerstromService;
    @Autowired
    AssessmentRepository assessmentRepository;

    /**
     * Luồng 1: Tạo kế hoạch theo đề xuất của hệ thống.
     */
    @Transactional
    public QuitPlanResponse createSystemGeneratedPlan(SystemPlanRequestDTO request) {
        Account account = authenticationService.getCurrentAccount();
        checkExistingPlan(account);
        Assessment assessment = assessmentRepository.findById(request.getAssessmentId())
                .orElseThrow(() -> new MyException("ID đánh giá không hợp lệ."));


        if (!assessment.getAccount().getUserId().equals(account.getUserId())) {
            throw new MyException("Không có quyền sử dụng kết quả đánh giá này.");
        }

        // Bước 2: Tạo plan với thông tin từ assessment
        int durationWeeks = determinePlanDuration(assessment.getDependencyLevel());
        QuitPlan plan = createAndPopulatePlan(request, account, assessment, durationWeeks);
        List<TaperingStep> schedule = generateStandardizedTaperingSchedule(
                assessment.getDependencyLevel(), plan.getStartDate(), plan.getCigarettesPerDay(), durationWeeks
        );
        plan.setTaperingSchedule(schedule);

        QuitPlan savedPlan = quitPlanRepository.save(plan);
        return mapPlanToResponse(savedPlan); // <-- Sử dụng hàm map tay cho response
    }
    /**
     * Luồng 2: Tạo kế hoạch do người dùng tùy chỉnh thời gian.
     */
    @Transactional
    public QuitPlanResponse createUserDefinedPlan(UserPlanRequestDTO request) {
        Account account = authenticationService.getCurrentAccount();
        checkExistingPlan(account);
        validateDuration(request.getDurationWeeks());

        Assessment assessment = assessmentRepository.findById(request.getAssessmentId())
                .orElseThrow(() -> new MyException("ID đánh giá không hợp lệ."));
        QuitPlan plan = createAndPopulatePlan(request, account, assessment, request.getDurationWeeks());
        List<TaperingStep> schedule = generateLinearTaperingSchedule(
                plan.getStartDate(), plan.getDurationWeeks(), plan.getCigarettesPerDay()
        );
        plan.setTaperingSchedule(schedule);

        QuitPlan savedPlan = quitPlanRepository.save(plan);
        return mapPlanToResponse(savedPlan); // <-- Sử dụng hàm map tay cho response
    }

    private List<TaperingStep> generateTaperingSchedule(
            java.time.LocalDate startDate,
            int durationWeeks,
            int startCigarettesPerDay
    ) {
        List<TaperingStep> steps = new ArrayList<>();

        // Tính số điếu giảm mỗi tuần
        int totalReduction = startCigarettesPerDay - 1; // Giảm xuống còn 1 điếu ở tuần cuối
        double decreasePerWeek = (double) totalReduction / durationWeeks;

        // Tạo các bước giảm dần theo tuần (từ tuần 1 đến tuần cuối)
        for (int i = 1; i <= durationWeeks; i++) {
            java.time.LocalDate weekStart = startDate.plusWeeks(i - 1);
            java.time.LocalDate weekEnd = startDate.plusWeeks(i).minusDays(1);

            // Tính số điếu cho tuần hiện tại
            int targetCigarettes = (int) Math.round(startCigarettesPerDay - (decreasePerWeek * i));

            // Đảm bảo tuần cuối còn 1 điếu
            if (i == durationWeeks) {
                targetCigarettes = 1;
            } else if (targetCigarettes < 1) {
                targetCigarettes = 1;
            }

            TaperingStep step = new TaperingStep(i, weekStart, weekEnd, targetCigarettes, null);
            steps.add(step);
        }

        // Thêm bước cuối - ngày bỏ thuốc hoàn toàn (ngày sau tuần cuối)
        java.time.LocalDate quitDate = startDate.plusDays(durationWeeks * 7);
        long dayNumber = java.time.temporal.ChronoUnit.DAYS.between(startDate, quitDate) + 1;
        TaperingStep quitStep = new TaperingStep(
                durationWeeks + 1,
                quitDate,
                quitDate,
                0,
                String.format("Từ ngày %s (ngày thứ %d) bạn sẽ bỏ thuốc hoàn toàn", quitDate, dayNumber)
        );
        steps.add(quitStep);

        return steps;
    }
    private QuitPlanResponse mapPlanToResponse(QuitPlan plan) {
        // 1. Khởi tạo đối tượng Response DTO rỗng
        QuitPlanResponse response = new QuitPlanResponse();

        // 2. Map các trường dữ liệu đơn giản
        response.setId(plan.getId());
        response.setStartDate(plan.getStartDate());
        response.setNumberOfCigarettes(plan.getCigarettesPerDay()); // Chú ý tên trường có thể khác nhau
        response.setPricePerPack(plan.getPricePerPack());
        response.setDurationWeeks(plan.getDurationWeeks());

        // 3. Map các trường chi phí đã được tính toán
        response.setDailyCost(plan.getDailyCost());
        response.setWeeklyCost(plan.getWeeklyCost());
        response.setMonthlyCost(plan.getMonthlyCost());
        response.setYearlyCost(plan.getYearlyCost());

        // 4. Map các danh sách (Collection)
        response.setReasons(plan.getReasons());
        response.setTriggers(plan.getTriggers());
        response.setSupportMethods(plan.getSupportMethods());
        response.setTaperingSchedule(plan.getTaperingSchedule());

        // 5. Map thông tin từ đối tượng Assessment liên quan
        // Luôn kiểm tra null để đảm bảo an toàn, tránh NullPointerException
        if (plan.getAssessment() != null) {
            response.setFagerstromScore(plan.getAssessment().getScore());
            response.setDependencyLevel(plan.getAssessment().getDependencyLevel());
        }

        // 6. Tạo danh sách 'tips' gợi ý
        // Giả sử bạn có các hàm suggestTips và suggestTipsForSupport
        List<String> allTips = new ArrayList<>();
        if (plan.getTriggers() != null && !plan.getTriggers().isEmpty()) {
            allTips.addAll(suggestTips(plan.getTriggers()));
        }
        if (plan.getSupportMethods() != null && !plan.getSupportMethods().isEmpty()) {
            allTips.addAll(suggestTipsForSupport(plan.getSupportMethods()));
        }
        response.setTips(allTips);

        // 7. Trả về đối tượng response hoàn chỉnh
        return response;
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

    private List<String> suggestTipsForSupport(Set<SupportMethod> supportMethods) {
        List<String> tips = new ArrayList<>();
        for (SupportMethod method : supportMethods) {
            switch (method) {
                // Support from people
                case PP_SHARE_WITH_IMPORTANT_PEOPLE:
                    tips.add("Chia sẻ kế hoạch cai thuốc với người thân và bạn bè để nhận được sự ủng hộ và động viên.");
                    break;
                case PP_FIND_QUIT_BUDDY:
                    tips.add("Tìm một người bạn cùng cai thuốc để có thể chia sẻ và hỗ trợ lẫn nhau.");
                    break;
                case PP_ASK_SUCCESSFUL_PEOPLE:
                    tips.add("Tìm gặp và học hỏi kinh nghiệm từ những người đã cai thuốc thành công.");
                    break;
                case PP_JOIN_ONLINE_COMMUNITY:
                    tips.add("Tham gia các cộng đồng online về cai thuốc để chia sẻ và nhận được lời khuyên hữu ích.");
                    break;
                case PP_REACH_OUT_OTHER:
                    tips.add("Chủ động liên hệ với người khác khi cảm thấy khó khăn trong quá trình cai thuốc.");
                    break;

                // Support from experts
                case EX_TALK_HEALTH_PROFESSIONAL:
                    tips.add("Trao đổi với bác sĩ hoặc chuyên gia y tế về kế hoạch cai thuốc của bạn.");
                    break;
                case EX_INPERSON_COUNSELING:
                    tips.add("Đăng ký tư vấn trực tiếp với chuyên gia để được hướng dẫn chi tiết.");
                    break;
                case EX_CALL_QUITLINE:
                    tips.add("Gọi đường dây nóng hỗ trợ cai thuốc khi cần được tư vấn ngay.");
                    break;
                case EX_SIGNUP_SMOKEFREE_TEXT:
                    tips.add("Đăng ký nhận tin nhắn động viên và lời khuyên hữu ích từ chương trình cai thuốc.");
                    break;
                case EX_DOWNLOAD_SMOKEFREE_APP:
                    tips.add("Tải ứng dụng hỗ trợ cai thuốc để theo dõi tiến trình và nhận lời khuyên.");
                    break;
                case EX_CHAT_ONLINE_COUNSELOR:
                    tips.add("Sử dụng dịch vụ chat online với chuyên gia tư vấn khi cần hỗ trợ.");
                    break;
                case EX_CONNECT_OTHER_EXPERTS:
                    tips.add("Kết nối với các chuyên gia khác trong lĩnh vực cai thuốc để nhận được nhiều góc nhìn.");
                    break;

                // Distraction methods
                case DI_DRINK_WATER:
                    tips.add("Uống nước thường xuyên, đặc biệt khi cảm thấy thèm thuốc.");
                    break;
                case DI_EAT_CRUNCHY_SNACK:
                    tips.add("Chuẩn bị các loại đồ ăn vặt giòn để ăn thay vì hút thuốc.");
                    break;
                case DI_DEEP_BREATHS:
                    tips.add("Tập các bài thở sâu khi cảm thấy căng thẳng hoặc thèm thuốc.");
                    break;
                case DI_EXERCISE:
                    tips.add("Tập thể dục đều đặn để giảm stress và cải thiện sức khỏe.");
                    break;
                case DI_PLAY_GAME_OR_LISTEN_MEDIA:
                    tips.add("Chơi game hoặc nghe nhạc để giải trí và quên đi cơn thèm thuốc.");
                    break;
                case DI_TEXT_OR_CALL_SUPPORTER:
                    tips.add("Nhắn tin hoặc gọi điện cho người hỗ trợ khi cảm thấy khó khăn.");
                    break;
                case DI_GO_TO_NONSMOKING_PLACE:
                    tips.add("Đến những nơi cấm hút thuốc để tránh cám dỗ.");
                    break;
                case DI_FIND_OTHER_DISTRACT:
                    tips.add("Tìm các hoạt động giải trí khác để chuyển hướng suy nghĩ khỏi thuốc lá.");
                    break;

                default:
                    tips.add("Hãy kiên trì với phương pháp bạn đã chọn và điều chỉnh nếu cần.");
            }
        }
        return tips;
    }

    public QuitPlanResponse getCurrentUserPlan() {
        Account account = authenticationService.getCurrentAccount();
        QuitPlan plan = quitPlanRepository.findByAccount(account)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kế hoạch cai thuốc cho người dùng này!"));

        QuitPlanResponse response = modelMapper.map(plan, QuitPlanResponse.class);
        // Ánh xạ thêm các trường cần thiết
        response.setTaperingSchedule(plan.getTaperingSchedule());
        response.setStartDate(plan.getStartDate());
        response.setDailyCost(plan.getDailyCost());
        response.setWeeklyCost(plan.getWeeklyCost());
        response.setMonthlyCost(plan.getMonthlyCost());
        response.setYearlyCost(plan.getYearlyCost());
        response.setNumberOfCigarettes(plan.getCigarettesPerDay());
        response.setPricePerPack(plan.getPricePerPack());
        response.setReasons(plan.getReasons());
        response.setTriggers(plan.getTriggers());
        response.setSupportMethods(plan.getSupportMethods());

        // Thêm tips dựa trên triggers và support methods
        List<String> allTips = new ArrayList<>();
        allTips.addAll(suggestTips(plan.getTriggers()));
        allTips.addAll(suggestTipsForSupport(plan.getSupportMethods()));
        response.setTips(allTips);

        return response;
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

    public QuitPlan getCurrentUserPlanEntity() {
        Account account = authenticationService.getCurrentAccount();
        return quitPlanRepository.findByAccount(account)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kế hoạch cai thuốc cho người dùng này!"));
    }

    public QuitPlanResponse getPlanById(Long planId) {
        Account account = authenticationService.getCurrentAccount();
        QuitPlan plan = quitPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kế hoạch cai thuốc!"));

        // Kiểm tra quyền truy cập
        if (plan.getAccount().getUserId() != account.getUserId()) {
            throw new RuntimeException("Bạn không có quyền xem kế hoạch này!");
        }

        QuitPlanResponse response = modelMapper.map(plan, QuitPlanResponse.class);
        // Map đầy đủ các trường
        response.setTaperingSchedule(plan.getTaperingSchedule());
        response.setStartDate(plan.getStartDate());
        response.setDailyCost(plan.getDailyCost());
        response.setWeeklyCost(plan.getWeeklyCost());
        response.setMonthlyCost(plan.getMonthlyCost());
        response.setYearlyCost(plan.getYearlyCost());
        response.setNumberOfCigarettes(plan.getCigarettesPerDay());
        response.setPricePerPack(plan.getPricePerPack());
        response.setReasons(plan.getReasons());
        response.setTriggers(plan.getTriggers());
        response.setSupportMethods(plan.getSupportMethods());

        // Thêm tips dựa trên triggers và support methods
        List<String> allTips = new ArrayList<>();
        allTips.addAll(suggestTips(plan.getTriggers()));
        allTips.addAll(suggestTipsForSupport(plan.getSupportMethods()));
        response.setTips(allTips);

        return response;
    }
    private QuitPlan createAndPopulatePlan(Object requestDTO, Account account, Assessment assessment, int duration) {
        QuitPlan plan = modelMapper.map(requestDTO, QuitPlan.class);
        plan.setAccount(account);
        plan.setAssessment(assessment);
        plan.setDurationWeeks(duration);
        calculateAndSetCosts(plan);
        return plan;
    }

    private void checkExistingPlan(Account account) {
        quitPlanRepository.findByAccount(account).ifPresent(p -> {
            throw new MyException("Bạn đã có kế hoạch. Vui lòng xóa kế hoạch cũ!");
        });
    }

    private void validateDuration(int weeks) {
        if (weeks < 2 || weeks > 6) {
            throw new MyException("Thời gian kế hoạch chỉ được phép từ 2 đến 6 tuần!");
        }
    }

    private int determinePlanDuration(DependencyLevel level) {
        switch (level) {
            case NHẸ: return 3;
            case TRUNG_BÌNH: return 4;
            case NẶNG: return 6;
            default: return 4;
        }
    }
    private QuitPlan createAndPopulatePlan(SystemPlanRequestDTO request, Account account, Assessment assessment, int duration) {
        QuitPlan plan = new QuitPlan();

        // Gán giá trị thủ công - an toàn và rõ ràng
        plan.setStartDate(request.getStartDate());
        plan.setCigarettesPerDay(request.getNumberOfCigarettes());
        plan.setPricePerPack(request.getPricePerPack());
        plan.setReasons(request.getReasons());
        plan.setTriggers(request.getTriggers());
        plan.setSupportMethods(request.getSupportMethods());

        // Gán các đối tượng liên quan
        plan.setAccount(account);
        plan.setAssessment(assessment);
        plan.setDurationWeeks(duration);

        // Gọi hàm tính toán
        calculateAndSetCosts(plan);

        return plan;
    }

    /**
     * Helper mới cho UserPlanRequestDTO (thay thế ModelMapper)
     */
    private QuitPlan createAndPopulatePlan(UserPlanRequestDTO request, Account account, Assessment assessment, int duration) {
        QuitPlan plan = new QuitPlan();

        // Gán giá trị thủ công
        plan.setStartDate(request.getStartDate());
        plan.setCigarettesPerDay(request.getNumberOfCigarettes());
        plan.setPricePerPack(request.getPricePerPack());
        plan.setReasons(request.getReasons());
        plan.setTriggers(request.getTriggers());
        plan.setSupportMethods(request.getSupportMethods());

        // Gán các đối tượng liên quan
        plan.setAccount(account);
        plan.setAssessment(assessment);
        plan.setDurationWeeks(duration);

        // Gọi hàm tính toán
        calculateAndSetCosts(plan);

        return plan;
    }

    private List<TaperingStep> generateStandardizedTaperingSchedule(DependencyLevel level, LocalDate startDate, int startCigarettes, int durationWeeks) {
        List<TaperingStep> steps = new ArrayList<>();
        double[] percentages;
        switch (level) {
            case NHẸ: percentages = new double[]{0.60, 0.30, 0.10}; break;
            case TRUNG_BÌNH: percentages = new double[]{0.75, 0.50, 0.25, 0.10}; break;
            default: case NẶNG: percentages = new double[]{0.85, 0.70, 0.55, 0.40, 0.20, 0.10}; break;
        }
        for (int i = 0; i < durationWeeks; i++) {
            int target = (int) Math.ceil(startCigarettes * percentages[i]);
            if (i == durationWeeks - 1 && target < 1) target = 1;
            steps.add(new TaperingStep(i + 1, startDate.plusWeeks(i), startDate.plusWeeks(i+1).minusDays(1), target, "Kế hoạch theo đề xuất hệ thống."));
        }
        steps.add(new TaperingStep(durationWeeks + 1, startDate.plusWeeks(durationWeeks), startDate.plusWeeks(durationWeeks), 0, "Ngày bỏ thuốc hoàn toàn!"));
        return steps;
    }

    private List<TaperingStep> generateLinearTaperingSchedule(LocalDate startDate, int durationWeeks, int startCigarettes) {
        List<TaperingStep> steps = new ArrayList<>();
        double totalReduction = startCigarettes > 1 ? startCigarettes - 1 : 0;
        double decreasePerWeek = durationWeeks > 0 ? totalReduction / durationWeeks : 0;
        for (int i = 1; i <= durationWeeks; i++) {
            int target = (int) Math.round(startCigarettes - (decreasePerWeek * i));
            if (target < 1 && i < durationWeeks) target = 1; else if (i == durationWeeks) target = 1;
            steps.add(new TaperingStep(i, startDate.plusWeeks(i - 1), startDate.plusWeeks(i).minusDays(1), target, "Kế hoạch tùy chỉnh."));
        }
        steps.add(new TaperingStep(durationWeeks + 1, startDate.plusWeeks(durationWeeks), startDate.plusWeeks(durationWeeks), 0, "Ngày bỏ thuốc hoàn toàn!"));
        return steps;
    }

    private void calculateAndSetCosts(QuitPlan plan) {
        BigDecimal dailyCost = plan.getPricePerPack()
                .multiply(BigDecimal.valueOf(plan.getCigarettesPerDay()))
                .divide(BigDecimal.valueOf(20), 2, RoundingMode.HALF_UP);
        plan.setDailyCost(dailyCost);
        plan.setWeeklyCost(dailyCost.multiply(BigDecimal.valueOf(7)));
        plan.setMonthlyCost(dailyCost.multiply(BigDecimal.valueOf(30)));
        plan.setYearlyCost(dailyCost.multiply(BigDecimal.valueOf(365)));
    }

    /**
     * Đếm số kế hoạch cai thuốc (tạm thời đếm tất cả)
     */
    public long countActivePlans() {
        return quitPlanRepository.countAll();
    }

    /**
     * Cập nhật trạng thái của kế hoạch
     */
    @Transactional
    public void updatePlanStatus(Account account, QuitPlanStatus status) {
        QuitPlan quitPlan = quitPlanRepository.findByAccount(account)
                .orElseThrow(() -> new ResourceNotFoundException("No quit plan found for this account"));
        quitPlan.setStatus(status);
        quitPlanRepository.save(quitPlan);
    }
}
