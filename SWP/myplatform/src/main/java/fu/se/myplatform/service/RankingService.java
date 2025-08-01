package fu.se.myplatform.service;

import fu.se.myplatform.dto.UserRankingDTO;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.QuitPlan;
import fu.se.myplatform.entity.SmokingRecord;
import fu.se.myplatform.repository.AccountRepository;
import fu.se.myplatform.repository.QuitPlanRepository;
import fu.se.myplatform.repository.SmokingRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RankingService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private QuitPlanRepository quitPlanRepository;

    @Autowired
    private SmokingRecordRepository smokingRecordRepository;

    // Ranking theo tiền tiết kiệm
    public List<UserRankingDTO> getMoneySavedRankings() {
        List<UserRankingDTO> rankings = new ArrayList<>();
        List<Account> accounts = accountRepository.findAll();

        for (Account account : accounts) {
            QuitPlan plan = quitPlanRepository.findByAccount(account).orElse(null);
            if (plan != null) {
                UserRankingDTO ranking = calculateMoneySavedRanking(account, plan);
                if (ranking.getTotalMoneySaved().compareTo(BigDecimal.ZERO) > 0) {
                    rankings.add(ranking);
                }
            }
        }

        // Sắp xếp theo tiền tiết kiệm được (cao đến thấp)
        rankings.sort((r1, r2) -> r2.getTotalMoneySaved().compareTo(r1.getTotalMoneySaved()));

        // Gán thứ hạng
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }

        return rankings;
    }

    public List<UserRankingDTO> getTopMoneySavedRankings(int limit) {
        return getMoneySavedRankings().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // Ranking theo độ tham gia
    public List<UserRankingDTO> getParticipationRankings() {
        List<UserRankingDTO> rankings = new ArrayList<>();
        List<Account> accounts = accountRepository.findAll();

        for (Account account : accounts) {
            QuitPlan plan = quitPlanRepository.findByAccount(account).orElse(null);
            if (plan != null) {
                UserRankingDTO ranking = calculateParticipationRanking(account, plan);
                if (ranking.getParticipationScore() > 0) {
                    rankings.add(ranking);
                }
            }
        }

        // Sắp xếp theo điểm tham gia (cao đến thấp)
        rankings.sort((r1, r2) -> Integer.compare(r2.getParticipationScore(), r1.getParticipationScore()));

        // Gán thứ hạng
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }

        return rankings;
    }

    public List<UserRankingDTO> getTopParticipationRankings(int limit) {
        return getParticipationRankings().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    private UserRankingDTO calculateMoneySavedRanking(Account account, QuitPlan plan) {
        UserRankingDTO dto = new UserRankingDTO();
        dto.setAccountId(account.getUserId());
        dto.setUsername(account.getUsername());
        dto.setAvatarUrl(account.getAvatarUrl());

        // Tính tổng tiền tiết kiệm được
        BigDecimal totalMoneySaved = calculateTotalMoneySaved(account, plan);
        dto.setTotalMoneySaved(totalMoneySaved);

        return dto;
    }

    private UserRankingDTO calculateParticipationRanking(Account account, QuitPlan plan) {
        UserRankingDTO dto = new UserRankingDTO();
        dto.setAccountId(account.getUserId());
        dto.setUsername(account.getUsername());
        dto.setAvatarUrl(account.getAvatarUrl());

        // Tính điểm tham gia
        int participationScore = calculateParticipationScore(account, plan);
        dto.setParticipationScore(participationScore);

        return dto;
    }

    private BigDecimal calculateTotalMoneySaved(Account account, QuitPlan plan) {
        List<SmokingRecord> records = smokingRecordRepository.findByAccountAndDateBetweenOrderByDateAsc(
            account, plan.getStartDate(), LocalDate.now());
        BigDecimal pricePerCigarette = plan.getPricePerPack()
                .divide(BigDecimal.valueOf(20), 2, java.math.RoundingMode.HALF_UP);

        int initialCigarettesPerDay = plan.getCigarettesPerDay();

        return records.stream()
                .map(record -> {
                    int savedCigarettes = initialCigarettesPerDay - record.getCigarettesSmoked();
                    if (savedCigarettes > 0) {
                        return pricePerCigarette.multiply(BigDecimal.valueOf(savedCigarettes));
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int calculateParticipationScore(Account account, QuitPlan plan) {
        LocalDate startDate = plan.getStartDate();
        LocalDate currentDate = LocalDate.now();
        List<SmokingRecord> records = smokingRecordRepository.findByAccountAndDateBetweenOrderByDateAsc(
                account, startDate, currentDate);

        // Tính số ngày đã tham gia ghi chép
        List<LocalDate> recordedDates = records.stream()
                .map(SmokingRecord::getDate)
                .sorted()
                .collect(Collectors.toList());

        if (recordedDates.isEmpty()) {
            return 0; // Chưa có ghi chép nào
        }

        // 1. Điểm cơ bản: mỗi lần ghi chép được 1 điểm
        int basePoints = 1;

        // 2. Điểm thưởng streak: tính số ngày liên tiếp ghi chép đến hiện tại
        int streakPoints = 0;

        // Chỉ tính streak nếu có ghi chép trong ngày hôm nay
        if (recordedDates.get(recordedDates.size() - 1).equals(currentDate)) {
            int streak = 1;
            LocalDate expectedDate = currentDate;

            // Đếm ngược từ ngày hiện tại
            for (int i = recordedDates.size() - 1; i > 0; i--) {
                LocalDate previousDate = recordedDates.get(i - 1);
                expectedDate = expectedDate.minusDays(1);

                if (previousDate.equals(expectedDate)) {
                    streak++;
                } else {
                    break;
                }
            }

            // Mỗi ngày trong streak được thêm 2 điểm
            streakPoints = streak * 2;
        }

        return basePoints + streakPoints;
    }
}
