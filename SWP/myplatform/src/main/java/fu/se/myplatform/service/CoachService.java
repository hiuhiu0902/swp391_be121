package fu.se.myplatform.service;

import fu.se.myplatform.dto.*;
import fu.se.myplatform.entity.*;
import fu.se.myplatform.repository.CoachRepository;
import fu.se.myplatform.repository.MemberRepository;
import fu.se.myplatform.repository.QuitPlanRepository;
import fu.se.myplatform.repository.SmokingRecordRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CoachService {
    @Autowired
    CoachRepository coachRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    SmokingRecordRepository smokingRecordRepository;
    @Autowired
    QuitPlanRepository quitPlanRepository;

    public Coach findById(Long coachId) {
        return coachRepository.findById(coachId)
                .orElseThrow(() -> new RuntimeException("Coach not found"));
    }

    public boolean hasCapacity(Long coachId) {
        // Count how many members are currently assigned to this coach
        long count = memberRepository.countByCoachId(coachId);
        return count < 5;
    }
    public List<MemberShortDTO> getMembersAssignedToCoach(Long coachId) {
        // Lấy Coach từ database theo coachId
        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new RuntimeException("Coach not found"));

        // Lấy danh sách các Member đã được phân cho Coach
        List<Member> members = coach.getMembers();

        // Chuyển đổi từ Member sang MemberShortDTO
        return members.stream()
                .map(member -> {
                    // Ánh xạ từ Member sang MemberShortDTO
                    MemberShortDTO memberShortDTO = new MemberShortDTO();
                    memberShortDTO.setMemberId(member.getMemberId());
                    memberShortDTO.setFullName(member.getUser().getFullName());
                    memberShortDTO.setStatus(member.getStatus());
                    memberShortDTO.setUserId(member.getUser().getUserId());
                    memberShortDTO.setAvatarUrl(member.getUser().getAvatarUrl());
                    return memberShortDTO;
                })
                .collect(Collectors.toList());
    }
    public List<SmokingRecordResponse> getMemberSmokingRecords(Long coachId, Long memberId) {
        // Xác thực xem coach có quyền xem member này không
        verifyCoachAccess(coachId, memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Member với ID: " + memberId));

        Account memberAccount = member.getUser();
        List<SmokingRecord> records = smokingRecordRepository.findByAccountOrderByDateDesc(memberAccount);

        // Chuyển đổi sang DTO để trả về
        return records.stream()
                .map(record -> modelMapper.map(record, SmokingRecordResponse.class))
                .collect(Collectors.toList());
    }

    /**
     * Helper method để kiểm tra xem Coach có được phân công cho Member không.
     */
    public WeeklyProgressStats getMemberWeeklyProgress(Long coachId, Long memberId, int weekNumber) {
        verifyCoachAccess(coachId, memberId);
        Account memberAccount = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Member với ID: " + memberId))
                .getUser();

        QuitPlan plan = quitPlanRepository.findByAccount(memberAccount)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kế hoạch cai thuốc cho người dùng này!"));

        LocalDate planStartDate = plan.getStartDate();
        LocalDate weekStartDate = planStartDate.plusWeeks(weekNumber - 1);
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        LocalDate today = LocalDate.now();

        int targetCigarettesPerDay = plan.getTaperingSchedule().stream()
                .filter(step -> step.getWeekNumber() == weekNumber)
                .findFirst()
                .map(TaperingStep::getCigarettesPerDay)
                .orElse(0);

        List<SmokingRecord> weeklyRecords = smokingRecordRepository.findByAccountAndDateBetweenOrderByDateAsc(memberAccount, weekStartDate, weekEndDate);

        WeeklyProgressStats stats = new WeeklyProgressStats();
        stats.setWeekNumber(weekNumber);
        stats.setWeekStartDate(weekStartDate);
        stats.setWeekEndDate(weekEndDate);
        stats.setTargetCigarettesPerDay(targetCigarettesPerDay);

        int totalSmoked = weeklyRecords.stream().mapToInt(SmokingRecord::getCigarettesSmoked).sum();
        stats.setTotalCigarettesSmoked(totalSmoked);

        List<WeeklyProgressStats.DailyProgress> dailyProgressList = new ArrayList<>();
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
                // There is a smoking record for this day
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
                // No smoking record for this day
                daily.setCigarettesSmoked(0);

                if (date.isAfter(today)) {
                    // Future date - no record yet
                    daily.setStatus("NO_RECORD");
                    // Don't count future days in statistics
                } else if (date.isBefore(today) || date.isEqual(today)) {
                    // Past date or today with no record - assume they didn't smoke
                    daily.setStatus("UNDER");
                    daysUnder++;
                } else {
                    // This shouldn't happen, but just in case
                    daily.setStatus("NO_RECORD");
                }
            }
            dailyProgressList.add(daily);
        }

        stats.setDaysOverTarget(daysOver);
        stats.setDaysOnTarget(daysOn);
        stats.setDaysUnderTarget(daysUnder);
        stats.setDailyProgress(dailyProgressList);

        return stats;
    }

    public List<WeeklyProgressStats> getMemberAllWeeksProgress(Long coachId, Long memberId) {
        verifyCoachAccess(coachId, memberId);
        Account memberAccount = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Member với ID: " + memberId))
                .getUser();

        QuitPlan plan = quitPlanRepository.findByAccount(memberAccount)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kế hoạch cai thuốc cho người dùng này!"));

        int totalWeeks = plan.getTaperingSchedule().size();
        List<WeeklyProgressStats> allStats = new ArrayList<>();
        for (int week = 1; week <= totalWeeks; week++) {
            allStats.add(getMemberWeeklyProgress(coachId, memberId, week));
        }
        return allStats;
    }

    private void verifyCoachAccess(Long coachId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Member với ID: " + memberId));

        if (member.getCoach() == null || !member.getCoach().getCoachId().equals(coachId)) {
            throw new RuntimeException("Bạn không có quyền truy cập vào dữ liệu của người dùng này.");
        }
    }
    @Transactional
    public QuitPlanResponse adjustNextWeekTarget(Long coachId, Long memberId, AdjustWeeklyTargetDTO request) {
        // Bước 1: Xác thực quyền của Coach
        verifyCoachAccess(coachId, memberId);

        // Bước 2: Tìm kế hoạch của Member
        Account memberAccount = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Member với ID: " + memberId))
                .getUser();
        QuitPlan plan = quitPlanRepository.findByAccount(memberAccount)
                .orElseThrow(() -> new RuntimeException("Member này chưa có kế hoạch cai thuốc."));

        // Bước 3: Logic "thông minh" để xác định tuần tiếp theo
        LocalDate today = LocalDate.now();
        LocalDate planStartDate = plan.getStartDate();

        if (today.isBefore(planStartDate)) {
            throw new RuntimeException("Kế hoạch chưa bắt đầu, không thể điều chỉnh tuần tiếp theo.");
        }

        // Tính toán số ngày đã trôi qua kể từ khi bắt đầu kế hoạch
        long daysFromStart = ChronoUnit.DAYS.between(planStartDate, today);
        // Xác định tuần hiện tại (chia cho 7, +1 vì tuần bắt đầu từ 1)
        int currentWeekNumber = (int) (daysFromStart / 7) + 1;
        int nextWeekNumber = currentWeekNumber + 1;

        // Bước 4: Tìm đúng tuần tiếp theo trong lịch trình
        TaperingStep stepToUpdate = plan.getTaperingSchedule().stream()
                .filter(step -> step.getWeekNumber() == nextWeekNumber && step.getCigarettesPerDay() >= 0) // Chấp nhận cả mục tiêu = 0
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tuần tiếp theo (Tuần " + nextWeekNumber + ") trong kế hoạch hoặc kế hoạch đã kết thúc."));

        // Bước 5: Cập nhật số điếu thuốc mục tiêu mới
        if (request.getNewTargetCigarettes() < 0) {
            throw new RuntimeException("Số điếu thuốc mục tiêu không thể là số âm.");
        }
        stepToUpdate.setCigarettesPerDay(request.getNewTargetCigarettes());
        stepToUpdate.setNote("Đã được Coach điều chỉnh vào ngày " + today); // Thêm ghi chú để người dùng biết

        // Bước 6: Lưu lại và trả về kết quả
        QuitPlan updatedPlan = quitPlanRepository.save(plan);
        return mapPlanToResponse(updatedPlan);
    }
    private QuitPlanResponse mapPlanToResponse(QuitPlan plan) {
        QuitPlanResponse response = new QuitPlanResponse();
        response.setId(plan.getId());
        response.setStartDate(plan.getStartDate());
        response.setNumberOfCigarettes(plan.getCigarettesPerDay());
        response.setPricePerPack(plan.getPricePerPack());
        response.setDurationWeeks(plan.getDurationWeeks());
        response.setDailyCost(plan.getDailyCost());
        response.setWeeklyCost(plan.getWeeklyCost());
        response.setMonthlyCost(plan.getMonthlyCost());
        response.setYearlyCost(plan.getYearlyCost());
        response.setReasons(plan.getReasons());
        response.setTriggers(plan.getTriggers());
        response.setSupportMethods(plan.getSupportMethods());
        response.setTaperingSchedule(plan.getTaperingSchedule());
        if (plan.getAssessment() != null) {
            response.setFagerstromScore(plan.getAssessment().getScore());
            response.setDependencyLevel(plan.getAssessment().getDependencyLevel());
        }
        return response;
    }

    @Transactional
    @Scheduled(cron = "0 0 2 * * *") // Runs at 2 AM every day
    public void unassignCompletedMembers() {
        List<Member> assignedMembers = memberRepository.findByCoachIsNotNull();

        if (assignedMembers.isEmpty()) {
            return;
        }

        for (Member member : assignedMembers) {
            Optional<QuitPlan> planOpt = quitPlanRepository.findByAccount(member.getUser());

            if (planOpt.isPresent()) {
                QuitPlan plan = planOpt.get();
                LocalDate startDate = plan.getStartDate();
                int durationInWeeks = plan.getTaperingSchedule().size();

                if (durationInWeeks > 0) {
                    LocalDate endDate = startDate.plusWeeks(durationInWeeks);

                    if (endDate.isBefore(LocalDate.now())) {
                        ;
                        member.setCoach(null);
                        member.setStatus("COMPLETED"); // Assuming 'status' can be updated to reflect this
                        memberRepository.save(member);
                    }
                }
            }
        }
    }
}
