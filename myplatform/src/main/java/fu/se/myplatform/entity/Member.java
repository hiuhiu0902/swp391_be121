package fu.se.myplatform.entity;

import jakarta.persistence.*;

public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public String memberId;

}
