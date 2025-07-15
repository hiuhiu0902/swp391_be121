package fu.se.myplatform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "staff")
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Long staffId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private Account user;

    @Column(name = "status", nullable = false)
    private String status = "active";

    @Column(name = "position")
    private String position;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = "active";
        }
    }
}
