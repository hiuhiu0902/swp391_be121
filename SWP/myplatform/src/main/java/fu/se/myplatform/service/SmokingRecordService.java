package fu.se.myplatform.service;

import fu.se.myplatform.dto.SmokingRecordRequest;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.SmokingRecord;
import fu.se.myplatform.repository.SmokingReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
public class SmokingRecordService {
    @Autowired
    private SmokingReportRepository smokingRecordRepository;

    @Autowired
    private AuthenticationService authenticationService;

    public SmokingRecord saveTodaySmoked(SmokingRecordRequest smokingRecord) {
        Account account = authenticationService.getCurrentAccount();
        LocalDate today = LocalDate.now();
        SmokingRecord record = smokingRecordRepository
                .findByAccountAndDate(account, today).orElse(null);

        if(record == null) {
           record = new SmokingRecord();
            record.setAccount(account);
            record.setDate(today);
        }
        record.setCigarettesSmoked(smokingRecord.getCigarettesSmoked());
        return smokingRecordRepository.save(record);
    }

    public List<SmokingRecord> getCurrentWeekRecords() {
        Account account = authenticationService.getCurrentAccount();
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);
        return smokingRecordRepository.findByAccountAndDateBetween(account, startOfWeek, endOfWeek);
    }

    //tong so luong thuoc trong tuan nao do
    public int getActualCigarettesSmokedInWeek(LocalDate weekStart, LocalDate weekEnd) {
        Account account = authenticationService.getCurrentAccount();
        List<SmokingRecord> records = smokingRecordRepository
                .findByAccountAndDateBetween(account, weekStart, weekEnd);
        return records.stream()
                .mapToInt(SmokingRecord::getCigarettesSmoked)
                .sum();
    }
}
