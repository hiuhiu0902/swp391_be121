package fu.se.myplatform.service;

import fu.se.myplatform.dto.MemberShortDTO;
import fu.se.myplatform.dto.SmokingRecordResponse;
import fu.se.myplatform.dto.TaperingStep;
import fu.se.myplatform.dto.WeeklyProgressStats;
import fu.se.myplatform.entity.*;
import fu.se.myplatform.repository.CoachRepository;
import fu.se.myplatform.repository.MemberRepository;
import fu.se.myplatform.repository.QuitPlanRepository;
import fu.se.myplatform.repository.SmokingRecordRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
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
}
