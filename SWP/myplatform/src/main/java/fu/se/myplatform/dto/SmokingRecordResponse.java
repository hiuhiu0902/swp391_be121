package fu.se.myplatform.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SmokingRecordResponse {
    private LocalDate date;
    private int cigarettesSmoked;
    private String message;  // Thông báo bổ sung (nếu cần)
}
