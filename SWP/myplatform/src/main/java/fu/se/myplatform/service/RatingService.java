package fu.se.myplatform.service;


import fu.se.myplatform.entity.Coach;
import fu.se.myplatform.entity.Member;
import fu.se.myplatform.entity.Rating;
import fu.se.myplatform.exception.exception.AuthenticationException;
import fu.se.myplatform.repository.CoachRepository;
import fu.se.myplatform.repository.MemberRepository;
import fu.se.myplatform.repository.RatingRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepository ratingRepository;
    private final MemberRepository memberRepository;
    private final CoachRepository coachRepository;

    public Rating addRating(Member member, Coach coach, int stars, String comment) {
        // Check đã từng rating chưa
        if (ratingRepository.existsByMember_MemberIdAndCoach_CoachId(member.getMemberId(), coach.getCoachId())) {
            throw new AuthenticationException("Bạn đã đánh giá huấn luyện viên này rồi!");
        }
        Rating rating = new Rating();
        rating.setMember(member);
        rating.setCoach(coach);
        rating.setStars(stars);
        rating.setComment(comment);
        return ratingRepository.save(rating);
    }

}
