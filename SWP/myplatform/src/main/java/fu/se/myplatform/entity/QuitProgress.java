package fu.se.myplatform.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class QuitProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quit_plan_id")
    private QuitPlan quitPlan;

    private int weekNumber; // Tuần số
    private LocalDate date; // Ngày khai báo
    private int targetCigarettes; // Số điếu tiêu chuẩn mỗi ngày
    private int actualCigarettes; // Số điếu thực tế user hút
    private boolean achieved; // Đạt chỉ tiêu hay không
    private String note;
    private String cigaretteType; // Loại thuốc hút
    private int packPrice; // Giá tiền 1 bao thuốc (VNĐ)

    // Getters, setters, constructors



}
