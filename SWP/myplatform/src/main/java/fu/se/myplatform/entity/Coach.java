package fu.se.myplatform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "coach")
public class Coach {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coach_id")
    private Long coachId;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private Account user;

    @Column(name = "status")
    private String status;

    @Column(name = "address")
    private String address;

    @OneToMany(mappedBy = "coach")
    @JsonIgnore
    private List<Member> members = new ArrayList<>();

    @OneToMany(mappedBy = "coach", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings;

    @Lob
    private byte[] profileImage; // Coach avatar

    @PreRemove
    private void preRemove() {
        // Remove coach reference from members before deleting coach
        for(Member member : members) {
            member.setCoach(null);
        }
        members.clear();

        // Clear all ratings
        ratings.clear();
    }
}