package fu.se.myplatform.dto;

import lombok.Data;

/**
 * DTO chứa số điếu thuốc mục tiêu mới.
 * Dùng cho việc Coach điều chỉnh kế hoạch tuần tiếp theo.
 */
@Data
public class AdjustWeeklyTargetDTO {
    private int newTargetCigarettes;
}