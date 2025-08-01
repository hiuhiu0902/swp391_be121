package fu.se.myplatform.api;

import fu.se.myplatform.dto.SmokingRecordRequest;
import fu.se.myplatform.dto.SmokingRecordResponse;
import fu.se.myplatform.dto.WeeklyProgressStats;
import fu.se.myplatform.entity.QuitPlan;
import fu.se.myplatform.entity.SmokingRecord;
import fu.se.myplatform.service.QuitPlanService;
import fu.se.myplatform.service.SmokingRecordService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
            @Valid @RequestBody SmokingRecordRequest request,
            @RequestParam(required = false) LocalDate date) {
        LocalDate recordDate = date != null ? date : LocalDateTime.now().toLocalDate();

        // Lấy record hiện tại của ngày (nếu có)
        SmokingRecord existingRecord = smokingRecordService.getRecordByDate(recordDate);

        // Nếu đã có record cho ngày này, cộng thêm số điếu mới
        if (existingRecord != null) {
            request.setCigarettesSmoked(request.getCigarettesSmoked() + existingRecord.getCigarettesSmoked());
        }

        SmokingRecord record = smokingRecordService.saveSmokingRecord(request, recordDate);
        SmokingRecordResponse response = new SmokingRecordResponse();
        response.setDate(record.getDate());
        response.setCigarettesSmoked(record.getCigarettesSmoked());

        // Thêm thông báo dựa vào so sánh với số điếu ban đầu
        QuitPlan plan = quitPlanService.getCurrentUserPlanEntity();
        int initialCigarettesPerDay = plan.getCigarettesPerDay();

        // Tính tiền tiết kiệm được
        int savedCigarettes = initialCigarettesPerDay - record.getCigarettesSmoked();
        if (savedCigarettes > 0) {
            // Tính giá một điếu thuốc = giá gói / 20 điếu
            BigDecimal pricePerCigarette = plan.getPricePerPack()
                .divide(BigDecimal.valueOf(20), 2, java.math.RoundingMode.HALF_UP);

            // Tính tiền tiết kiệm được hôm nay
            BigDecimal moneySavedToday = pricePerCigarette.multiply(BigDecimal.valueOf(savedCigarettes));
            response.setMoneySaved(moneySavedToday);
            
            // Tính tổng tiền tiết kiệm được từ trước đến nay
            LocalDate startDate = plan.getStartDate();
            List<SmokingRecord> allRecords = smokingRecordService.getRecordsByDateRange(startDate, recordDate);
            BigDecimal totalMoneySaved = allRecords.stream()
                .map(r -> {
                    int dailySaved = initialCigarettesPerDay - r.getCigarettesSmoked();
                    return dailySaved > 0 ?
                        pricePerCigarette.multiply(BigDecimal.valueOf(dailySaved)) : 
                        BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            response.setTotalMoneySaved(totalMoneySaved);
        } else {
            response.setMoneySaved(BigDecimal.ZERO);
            response.setTotalMoneySaved(BigDecimal.ZERO);
        }

        if (record.getCigarettesSmoked() > initialCigarettesPerDay) {
            response.setMessage("Bạn đã hút vượt quá số điếu hút ban đầu của bạn!");
        } else if (record.getCigarettesSmoked() == initialCigarettesPerDay) {
            response.setMessage("Hôm nay bạn hút bằng với số điếu ban đầu.");
        } else {
            response.setMessage("Xuất sắc! Bạn đã hút ít hơn số điếu ban đầu và tiết kiệm được "
                + String.format("%,.0f", response.getMoneySaved().doubleValue()) + " VNĐ!");
        }
        return ResponseEntity.ok(response);
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

        // Tính tiền tiết kiệm được
        QuitPlan plan = quitPlanService.getCurrentUserPlanEntity();
        int initialCigarettesPerDay = plan.getCigarettesPerDay();
        int savedCigarettes = initialCigarettesPerDay - record.getCigarettesSmoked();

        if (savedCigarettes > 0) {
            BigDecimal pricePerCigarette = plan.getPricePerPack()
                .divide(BigDecimal.valueOf(20), 2, java.math.RoundingMode.HALF_UP);

            BigDecimal moneySavedToday = pricePerCigarette.multiply(BigDecimal.valueOf(savedCigarettes));
            response.setMoneySaved(moneySavedToday);

            // Tính tổng tiền tiết kiệm được đến ngày này
            LocalDate startDate = plan.getStartDate();
            List<SmokingRecord> allRecords = smokingRecordService.getRecordsByDateRange(startDate, date);
            BigDecimal totalMoneySaved = allRecords.stream()
                .map(r -> {
                    int dailySaved = initialCigarettesPerDay - r.getCigarettesSmoked();
                    return dailySaved > 0 ?
                        pricePerCigarette.multiply(BigDecimal.valueOf(dailySaved)) :
                        BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            response.setTotalMoneySaved(totalMoneySaved);
        } else {
            response.setMoneySaved(BigDecimal.ZERO);
            response.setTotalMoneySaved(BigDecimal.ZERO);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<SmokingRecordResponse>> getRecordsByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body(null);
        }
        List<SmokingRecord> records = smokingRecordService.getRecordsByDateRange(startDate, endDate);
        QuitPlan plan = quitPlanService.getCurrentUserPlanEntity();

        BigDecimal pricePerCigarette = plan.getPricePerPack()
            .divide(BigDecimal.valueOf(20), 2, java.math.RoundingMode.HALF_UP);

        List<SmokingRecordResponse> responseList = records.stream()
            .map(record -> {
                SmokingRecordResponse res = new SmokingRecordResponse();
                res.setDate(record.getDate());
                res.setCigarettesSmoked(record.getCigarettesSmoked());

                // Tính tiền tiết kiệm được cho từng ngày
                int initialCigarettesPerDay = plan.getCigarettesPerDay();
                int savedCigarettes = initialCigarettesPerDay - record.getCigarettesSmoked();
                if (savedCigarettes > 0) {
                    BigDecimal moneySavedToday = pricePerCigarette.multiply(BigDecimal.valueOf(savedCigarettes));
                    res.setMoneySaved(moneySavedToday);

                    // Tính tổng tiền tiết kiệm được đến ngày này
                    List<SmokingRecord> recordsToDate = smokingRecordService.getRecordsByDateRange(plan.getStartDate(), record.getDate());
                    BigDecimal totalMoneySaved = recordsToDate.stream()
                        .map(r -> {
                            int dailySaved = initialCigarettesPerDay - r.getCigarettesSmoked();
                            return dailySaved > 0 ?
                                pricePerCigarette.multiply(BigDecimal.valueOf(dailySaved)) :
                                BigDecimal.ZERO;
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    res.setTotalMoneySaved(totalMoneySaved);
                } else {
                    res.setMoneySaved(BigDecimal.ZERO);
                    res.setTotalMoneySaved(BigDecimal.ZERO);
                }
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
            if (quitPlan == null) {
                return ResponseEntity.badRequest().body(null);
            }

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

    @DeleteMapping("/delete/{date}")
    public ResponseEntity<?> deleteRecord(@RequestParam(required = false) String date) {
        try {
            LocalDate localDate = LocalDateTime.now().toLocalDate(); // Chuyển string thành LocalDate
            smokingRecordService.deleteRecord(localDate);
            return ResponseEntity.ok("Đã xóa record ngày " + localDate + " thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Không thể xóa record. Lỗi: " + e.getMessage());
        }
    }
}