package fu.se.myplatform.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SmokingRecordResponse {
    private LocalDate date;
    private int cigarettesSmoked;
    private String message;  // Thông báo bổ sung (nếu cần)
    private BigDecimal moneySaved;  // Số tiền tiết kiệm được
    private BigDecimal totalMoneySaved;  // Tổng số tiền tiết kiệm được tính đến ngày hiện tại
}
