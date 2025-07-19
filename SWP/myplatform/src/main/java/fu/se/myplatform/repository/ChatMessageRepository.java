package fu.se.myplatform.repository;

import fu.se.myplatform.entity.ChatMessage;
import fu.se.myplatform.entity.Coach;
import fu.se.myplatform.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
        // Tìm tất cả tin nhắn giữa member và coach theo ID
        List<ChatMessage> findByMember_MemberIdAndCoach_CoachIdOrderBySentAtAsc(Long memberId, Long coachId);

        // Thống kê số lượng tin nhắn của coach trong một khoảng thời gian
        long countByCoachAndSentAtBetween(Coach coach, LocalDateTime start, LocalDateTime end);

        // Lấy tin nhắn mới nhất của coach với member
        ChatMessage findTop1ByCoachAndMemberOrderBySentAtDesc(Coach coach, Member member);


}
