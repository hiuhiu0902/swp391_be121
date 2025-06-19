package fu.se.myplatform.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
public class QuitPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "member_id", unique = true)
    private Member member;

    private int totalWeeks; // Số tuần cai thuốc
    private int initialCigarettes; // Số điếu ban đầu mỗi ngày
    private LocalDate startDate;
    private String status; // active, completed, failed
    private int packPrice; // Giá tiền 1 bao thuốc (VNĐ)

    @OneToMany(mappedBy = "quitPlan", cascade = CascadeType.ALL)
    private List<QuitProgress> progresses;

    // Getters, setters, constructors
    public QuitPlan() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
    public int getTotalWeeks() { return totalWeeks; }
    public void setTotalWeeks(int totalWeeks) { this.totalWeeks = totalWeeks; }
    public int getInitialCigarettes() { return initialCigarettes; }
    public void setInitialCigarettes(int initialCigarettes) { this.initialCigarettes = initialCigarettes; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getPackPrice() { return packPrice; }
    public void setPackPrice(int packPrice) { this.packPrice = packPrice; }
    public List<QuitProgress> getProgresses() { return progresses; }
    public void setProgresses(List<QuitProgress> progresses) { this.progresses = progresses; }
}
