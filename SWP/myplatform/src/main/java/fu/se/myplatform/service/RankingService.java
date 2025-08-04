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

    @Autowired
    private SmokingRecordService smokingRecordService;

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

    private int calculateConsecutiveDays(Account account) {
        List<SmokingRecord> allRecords = smokingRecordRepository.findByAccountOrderByDateDesc(account);
        if (allRecords.isEmpty()) {
            return 0;
        }

        int maxConsecutiveDays = 1;
        int currentStreak = 1;

        for (int i = 0; i < allRecords.size() - 1; i++) {
            LocalDate currentDate = allRecords.get(i).getDate();
            LocalDate nextDate = allRecords.get(i + 1).getDate();

            if (currentDate.minusDays(1).equals(nextDate)) {
                currentStreak++;
                maxConsecutiveDays = Math.max(maxConsecutiveDays, currentStreak);
            } else {
                currentStreak = 1;
            }
        }

        return maxConsecutiveDays;
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
                    // Thêm số ngày liên tục làm tiêu chí phụ
                    ranking.setSecondaryScore(calculateConsecutiveDays(account));
                    rankings.add(ranking);
                }
            }
        }

        // Sắp xếp theo điểm tham gia (cao đến thấp) và số ngày liên tục
        rankings.sort((r1, r2) -> {
            int compareResult = Integer.compare(r2.getParticipationScore(), r1.getParticipationScore());
            if (compareResult == 0) {
                // Nếu điểm tham gia bằng nhau, so sánh theo số ngày liên tục
                return Integer.compare(r2.getSecondaryScore(), r1.getSecondaryScore());
            }
            return compareResult;
        });

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

    private UserRankingDTO calculateParticipationRanking(Account account, QuitPlan plan) {
        UserRankingDTO dto = new UserRankingDTO();
        dto.setAccountId(account.getUserId());
        dto.setUsername(account.getUsername());
        dto.setAvatarUrl(account.getAvatarUrl());

        LocalDate startDate = plan.getStartDate();
        // Lấy ngày của record gần nhất thay vì dùng ngày hiện tại
        LocalDate lastRecordDate = smokingRecordRepository
                .findFirstByAccountOrderByDateDesc(account)
                .map(SmokingRecord::getDate)
                .orElse(startDate);

        List<SmokingRecord> records = smokingRecordRepository.findByAccountAndDateBetweenOrderByDateAsc(
            account, startDate, lastRecordDate
        );

        // Tính tổng điểm tham gia
        int totalPoints = 0;
        for (SmokingRecord record : records) {
            totalPoints += smokingRecordService.calculateParticipationPoints(record, plan);
        }

        dto.setParticipationScore(totalPoints);
        return dto;
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

    private BigDecimal calculateTotalMoneySaved(Account account, QuitPlan plan) {
        // Lấy ngày của record gần nhất thay vì dùng ngày hiện tại
        LocalDate lastRecordDate = smokingRecordRepository
                .findFirstByAccountOrderByDateDesc(account)
                .map(SmokingRecord::getDate)
                .orElse(plan.getStartDate());

        List<SmokingRecord> records = smokingRecordRepository.findByAccountAndDateBetweenOrderByDateAsc(
            account, plan.getStartDate(), lastRecordDate);

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
}
