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
    List<ChatMessage> findByMemberAndCoachOrderBySentAtAsc(Member member, Coach coach);

    // Thống kê: đếm tin nhắn của coach gửi ra trong ngày
    long countByCoachAndSentAtBetween(Coach coach, LocalDateTime start, LocalDateTime end);

    // Lấy tin nhắn mới nhất của coach với member
    ChatMessage findTop1ByCoachAndMemberOrderBySentAtDesc(Coach coach, Member member);
}
