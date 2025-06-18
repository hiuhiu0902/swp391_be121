package fu.se.myplatform.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
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
    public QuitProgress() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public QuitPlan getQuitPlan() { return quitPlan; }
    public void setQuitPlan(QuitPlan quitPlan) { this.quitPlan = quitPlan; }
    public int getWeekNumber() { return weekNumber; }
    public void setWeekNumber(int weekNumber) { this.weekNumber = weekNumber; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public int getTargetCigarettes() { return targetCigarettes; }
    public void setTargetCigarettes(int targetCigarettes) { this.targetCigarettes = targetCigarettes; }
    public int getActualCigarettes() { return actualCigarettes; }
    public void setActualCigarettes(int actualCigarettes) { this.actualCigarettes = actualCigarettes; }
    public boolean isAchieved() { return achieved; }
    public void setAchieved(boolean achieved) { this.achieved = achieved; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getCigaretteType() { return cigaretteType; }
    public void setCigaretteType(String cigaretteType) { this.cigaretteType = cigaretteType; }
    public int getPackPrice() { return packPrice; }
    public void setPackPrice(int packPrice) { this.packPrice = packPrice; }
}
