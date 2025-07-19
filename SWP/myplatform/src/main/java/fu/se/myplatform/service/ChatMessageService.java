package fu.se.myplatform.service;

import fu.se.myplatform.dto.ChatMessageRequest;
import fu.se.myplatform.dto.ChatMessageResponse;
import fu.se.myplatform.dto.UserBasicInfoResponse;
import fu.se.myplatform.entity.ChatMessage;
import fu.se.myplatform.entity.Coach;
import fu.se.myplatform.entity.Member;
import fu.se.myplatform.repository.ChatMessageRepository;
import fu.se.myplatform.repository.CoachRepository;
import fu.se.myplatform.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatMessageService {
    @Autowired
    ChatMessageRepository chatMessageRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    CoachRepository coachRepository;

    // Kiểm tra member-coach đã assign nhau chưa
    public boolean isAssigned(Long memberId, Long coachId) {
        Member member = memberRepository.findById(memberId).orElse(null);
        return member != null && member.getCoach() != null && member.getCoach().getCoachId().equals(coachId);
    }

    public ChatMessageResponse saveMessage(ChatMessageRequest request) {
        if (!isAssigned(request.getMemberId(), request.getCoachId()))
            throw new RuntimeException("Member chưa assign với coach này!");

        Member member = memberRepository.findById(request.getMemberId()).orElseThrow();
        Coach coach = coachRepository.findById(request.getCoachId()).orElseThrow();

        ChatMessage chat = new ChatMessage();
        chat.setContent(request.getContent());
        chat.setSentAt(LocalDateTime.now());
        chat.setSenderIsCoach(request.isSenderIsCoach());
        chat.setMember(member);
        chat.setCoach(coach);

        // Không cần set status nữa
        chat = chatMessageRepository.save(chat);
        if (chat == null) {
            throw new RuntimeException("Failed to save message.");
        }

        ChatMessageResponse response = new ChatMessageResponse();
        response.setId(chat.getId());
        response.setContent(chat.getContent());
        response.setSentAt(chat.getSentAt());
        response.setSenderIsCoach(chat.isSenderIsCoach());
        response.setMemberId(chat.getMember().getMemberId());
        response.setCoachId(chat.getCoach().getCoachId());
        response.setSenderName(
                chat.isSenderIsCoach() ? chat.getCoach().getUser().getFullName() : chat.getMember().getUser().getFullName()
        );
        return response;
    }

    @Transactional
    public List<ChatMessageResponse> getChatHistory(Long memberId, Long coachId) {
        List<ChatMessage> messages = chatMessageRepository
                .findByMember_MemberIdAndCoach_CoachIdOrderBySentAtAsc(memberId, coachId);

        List<ChatMessageResponse> response = new ArrayList<>();
        for (ChatMessage message : messages) {
            ChatMessageResponse res = new ChatMessageResponse();
            res.setId(message.getId());
            res.setContent(message.getContent());
            res.setSentAt(message.getSentAt());
            res.setSenderIsCoach(message.isSenderIsCoach());
            res.setMemberId(message.getMember().getMemberId());  // Truy cập dữ liệu mà không gây LazyInitializationException
            res.setCoachId(message.getCoach().getCoachId());
            res.setSenderName(message.isSenderIsCoach() ? message.getCoach().getUser().getFullName() : message.getMember().getUser().getFullName());
            response.add(res);
        }
        return response;
    }




    // Danh sách coach member được chat (gần như luôn chỉ 1 coach)
    public List<UserBasicInfoResponse> getAssignableCoaches(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        Coach coach = member.getCoach();
        if (coach == null) return List.of();
        UserBasicInfoResponse coachInfo = new UserBasicInfoResponse();
        coachInfo.setId(coach.getCoachId());
        coachInfo.setFullName(coach.getUser().getFullName());
        coachInfo.setAvatarUrl(null); // bổ sung nếu dùng avatar
        return List.of(coachInfo);
    }

    // Danh sách member coach được chat (các member đã assign coach này)
    public List<UserBasicInfoResponse> getAssignableMembers(Long coachId) {
        Coach coach = coachRepository.findById(coachId).orElseThrow();
        return coach.getMembers().stream().map(member -> {
            UserBasicInfoResponse memberInfo = new UserBasicInfoResponse();
            memberInfo.setId(member.getMemberId());
            memberInfo.setFullName(member.getUser().getFullName());
            memberInfo.setAvatarUrl(null);
            return memberInfo;
        }).collect(Collectors.toList());
    }
}
