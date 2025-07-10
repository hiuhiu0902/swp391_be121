package fu.se.myplatform.repository;

import fu.se.myplatform.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT m FROM ChatMessage m WHERE (m.senderId = :user1 AND m.receiverId = :user2) OR (m.senderId = :user2 AND m.receiverId = :user1) ORDER BY m.timestamp")
    List<ChatMessage> findChatHistory(@Param("user1") Long user1, @Param("user2") Long user2);

    List<ChatMessage> findBySenderIdAndReceiverIdOrderByTimestamp(Long senderId, Long receiverId);
}
