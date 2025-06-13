package fu.se.myplatform.entity;

import jakarta.persistence.*;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    public String memberId;

    @OneToOne
    @JoinColumn(name = "account_id")
    Account account;

}
