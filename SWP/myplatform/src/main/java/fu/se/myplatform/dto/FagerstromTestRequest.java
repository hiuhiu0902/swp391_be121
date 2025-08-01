package fu.se.myplatform.dto;

import lombok.Data;

@Data
public class FagerstromTestRequest {
    // Giá trị là số điểm của câu trả lời được chọn (ví dụ: 0, 1, 2, hoặc 3)
    private int answer1; // Bạn hút điếu thuốc đầu tiên sau khi thức dậy bao lâu?
    private int answer2; // Bạn có cảm thấy khó khăn trong việc kiềm chế hút thuốc ở những nơi bị cấm không?
    private int answer3; // Điếu thuốc nào bạn cảm thấy khó bỏ nhất?
    private int answer4; // Bạn hút bao nhiêu điếu thuốc mỗi ngày?
    private int answer5; // Bạn có hút thuốc thường xuyên hơn vào những giờ đầu tiên...?
    private int answer6; // Bạn có tiếp tục hút thuốc ngay cả khi bị bệnh nặng không?
}
