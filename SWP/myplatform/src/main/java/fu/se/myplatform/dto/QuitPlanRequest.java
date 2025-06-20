package fu.se.myplatform.dto;

import fu.se.myplatform.enums.QuitReason;
import fu.se.myplatform.enums.Triggers;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Data
public class QuitPlanRequest {

    public LocalDate startDate; // ngày bắt đầu cai
    public int durationWeeks; // số tuần giảm dần về 0
    public int numberOfCigarettes; // số điếu hút/ngày hiện tại
    public BigDecimal pricePerPack; // giá 1 gói thuốc
    public List<QuitReason> reasons; // danh sách lý do
    public List<Triggers> triggers; // danh sách triggers
}
