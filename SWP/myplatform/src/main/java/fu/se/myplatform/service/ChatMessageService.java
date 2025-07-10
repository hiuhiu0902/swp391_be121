package fu.se.myplatform.service;

import fu.se.myplatform.entity.ChatMessage;
import fu.se.myplatform.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageRepository repo;

    // Lưu tin nhắn
    public ChatMessage save(ChatMessage chat) {
        return repo.save(chat);
    }

    // Lấy lịch sử chat giữa 2 user (có thể là vipmember-coach hoặc ngược lại)
    public List<ChatMessage> getHistory(Long senderId, Long receiverId) {
        // Lấy cả 2 chiều
        List<ChatMessage> history = repo.findBySenderIdAndReceiverIdOrderByTimestamp(senderId, receiverId);
        history.addAll(repo.findBySenderIdAndReceiverIdOrderByTimestamp(receiverId, senderId));
        // Sắp xếp lại theo timestamp nếu cần
        history.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
        return history;
    }
}
