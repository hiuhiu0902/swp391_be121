package fu.se.myplatform.api;

import fu.se.myplatform.dto.SmokingRecordRequest;
import fu.se.myplatform.dto.SmokingRecordResponse;
import fu.se.myplatform.entity.SmokingRecord;
import fu.se.myplatform.service.QuitPlanService;
import fu.se.myplatform.service.SmokingRecordService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/today")
    public ResponseEntity recordToday(@RequestBody SmokingRecordRequest smokingRecordRequest) {
        SmokingRecord smokingRecord = smokingRecordService.saveTodaySmoked(smokingRecordRequest);
        SmokingRecordResponse smokingRecordResponse = new SmokingRecordResponse();
        smokingRecordResponse.setCiagrettesSmoked(smokingRecordResponse.getCiagrettesSmoked());
        smokingRecordResponse.setDate(smokingRecord.getDate());
        return ResponseEntity.ok(smokingRecordResponse);
    }

    @GetMapping("/this-week")
    public ResponseEntity getThisWeekSmokingRecords() {
        List<SmokingRecord> list = smokingRecordService.getCurrentWeekRecords();

        List<SmokingRecordResponse> responseList = list.stream().map(record -> {
            SmokingRecordResponse res = new SmokingRecordResponse();
            res.setDate(record.getDate());
            res.setCiagrettesSmoked(record.getCigarettesSmoked());
            return res;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }
}