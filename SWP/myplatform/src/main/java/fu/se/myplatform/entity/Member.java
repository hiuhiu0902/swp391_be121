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

    @Column(name = "is_actived")
    public boolean isActived;

    @ManyToOne
    @JoinColumn(name = "coach_id")
    private Coach coach;

    // Ảnh đại diện (có thể giữ lại profileImage, hoặc thêm avatarUrl nếu lưu ngoài server)
    @Lob
    @Column(name = "profile_image")
    private byte[] profileImage;

    // Đánh giá Coach (member có thể đánh giá coach, 1-5 sao + comment)
    @Column(name = "coach_rating")
    private Integer coachRating; // số sao 1-5

    @Column(name = "coach_rating_comment", columnDefinition = "TEXT")
    private String coachRatingComment;
    // Getters and setters


}
