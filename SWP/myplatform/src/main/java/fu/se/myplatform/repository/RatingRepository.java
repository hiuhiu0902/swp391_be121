package fu.se.myplatform.repository;

import fu.se.myplatform.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    // Kiểm tra member đã đánh giá coach này chưa (chỉ được 1 lần)
    boolean existsByMemberIdAndCoachId(Long memberId, Long coachId);
}
