package fu.se.myplatform.service;


import fu.se.myplatform.entity.Coach;
import fu.se.myplatform.entity.Member;
import fu.se.myplatform.entity.Rating;
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

    public Rating addRating(Long memberId, Long coachId, int stars, String comment) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new RuntimeException("Coach not found"));
        // Check if member is allowed to rate this coach (only their own coach in this example)
        if (member.getCoach() == null || !member.getCoach().getCoachId().equals(coachId)) {
            throw new RuntimeException("Member cannot rate this coach");
        }
        // Check for duplicate rating by the same member for the same coach
        // (requires RatingRepository.existsByMemberIdAndCoachId to be defined)
        if (ratingRepository.existsByMemberIdAndCoachId(memberId, coachId)) {
            throw new RuntimeException("Member has already rated this coach");
        }
        Rating rating = new Rating();
        rating.setMember(member);
        rating.setCoach(coach);
        rating.setStars(stars);
        rating.setComment(comment);
        return ratingRepository.save(rating);
    }
}
