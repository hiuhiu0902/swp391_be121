package fu.se.myplatform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Entity
@Table(name = "member")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private Account user;

    @Column(name = "status")
    private String status;

    @Column(name = "is_vip")
    private Boolean isVip;

    @Column(name = "vip_start_date")
    private LocalDate vipStartDate;

    @Column(name = "vip_expiry_date")
    private LocalDate vipExpiryDate;

    // Getters and setters

}
