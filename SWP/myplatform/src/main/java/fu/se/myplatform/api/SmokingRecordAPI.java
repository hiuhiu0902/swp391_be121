package fu.se.myplatform.api;

import fu.se.myplatform.dto.SmokingRecordRequest;
import fu.se.myplatform.dto.SmokingRecordResponse;
import fu.se.myplatform.dto.WeeklyProgressStats;
import fu.se.myplatform.entity.QuitPlan;
import fu.se.myplatform.entity.SmokingRecord;
import fu.se.myplatform.service.QuitPlanService;
import fu.se.myplatform.service.SmokingRecordService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@SecurityRequirement(name = "api")
@RequestMapping("/api/smoking-records")
public class SmokingRecordAPI {
    @Autowired
    SmokingRecordService smokingRecordService;

    @Autowired
    QuitPlanService quitPlanService;

    @PostMapping("/record")
    public ResponseEntity<SmokingRecordResponse> recordSmokingData(
            @RequestBody SmokingRecordRequest request,
            @RequestParam(required = false) LocalDate date) {
        SmokingRecord record = smokingRecordService.saveSmokingRecord(request, date != null ? date : LocalDate.now());
        SmokingRecordResponse response = new SmokingRecordResponse();
        response.setDate(record.getDate());
        response.setCigarettesSmoked(record.getCigarettesSmoked());

        // Thêm thông báo dựa vào so sánh với mục tiêu
        QuitPlan plan = quitPlanService.getCurrentUserPlanEntity();
        int targetForWeek = smokingRecordService.getTargetForDate(record.getDate(), plan);
        if (record.getCigarettesSmoked() > targetForWeek) {
            response.setMessage("Bạn đã hút vượt quá mục tiêu của ngày hôm nay!");
        } else if (record.getCigarettesSmoked() == targetForWeek) {
            response.setMessage("Tuyệt vời! Bạn đã đạt đúng mục tiêu của ngày hôm nay.");
        } else {
            response.setMessage("Xuất sắc! Bạn đã hút ít hơn mục tiêu đề ra.");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current-plan-week")
    public ResponseEntity<List<SmokingRecordResponse>> getCurrentPlanWeekRecords(
            @RequestParam(required = false) Integer weekNumber) {
        List<SmokingRecord> records = smokingRecordService.getPlanWeekRecords(weekNumber);
        List<SmokingRecordResponse> responseList = records.stream()
            .map(record -> {
                SmokingRecordResponse res = new SmokingRecordResponse();
                res.setDate(record.getDate());
                res.setCigarettesSmoked(record.getCigarettesSmoked());
                return res;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<SmokingRecordResponse> getRecordByDate(
            @PathVariable LocalDate date) {
        SmokingRecord record = smokingRecordService.getRecordByDate(date);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        SmokingRecordResponse response = new SmokingRecordResponse();
        response.setDate(record.getDate());
        response.setCigarettesSmoked(record.getCigarettesSmoked());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<SmokingRecordResponse>> getRecordsByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<SmokingRecord> records = smokingRecordService.getRecordsByDateRange(startDate, endDate);
        List<SmokingRecordResponse> responseList = records.stream()
            .map(record -> {
                SmokingRecordResponse res = new SmokingRecordResponse();
                res.setDate(record.getDate());
                res.setCigarettesSmoked(record.getCigarettesSmoked());
                return res;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/progress/week/{weekNumber}")
    public ResponseEntity<WeeklyProgressStats> getWeekProgress(
            @PathVariable int weekNumber) {
        try {
            WeeklyProgressStats stats = smokingRecordService.getWeeklyProgress(weekNumber);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/progress/all-weeks")
    public ResponseEntity<List<WeeklyProgressStats>> getAllWeeksProgress() {
        try {
            QuitPlan quitPlan = quitPlanService.getCurrentUserPlanEntity();
            int totalWeeks = quitPlan.getTaperingSchedule().size() - 1;
            List<WeeklyProgressStats> allStats = new ArrayList<>();
            for (int week = 1; week <= totalWeeks; week++) {
                allStats.add(smokingRecordService.getWeeklyProgress(week));
            }
            return ResponseEntity.ok(allStats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}