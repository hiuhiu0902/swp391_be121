package fu.se.myplatform.service;

import fu.se.myplatform.dto.ChatMessageDTO;
import fu.se.myplatform.entity.ChatMessage;
import fu.se.myplatform.entity.Coach;
import fu.se.myplatform.entity.Member;
import fu.se.myplatform.exception.exception.AuthenticationException;
import fu.se.myplatform.repository.ChatMessageRepository;
import fu.se.myplatform.repository.CoachRepository;
import fu.se.myplatform.repository.MemberRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @Autowired
    ModelMapper modelMapper;

    // Lưu tin nhắn
    public ChatMessageDTO sendMessage(ChatMessageDTO dto, long senderUserId) {
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new AuthenticationException("Member Not Found"));
        Coach coach = coachRepository.findById(dto.getCoachId())
                .orElseThrow(() -> new AuthenticationException("Coach Not Found"));

        if (!member.getCoach().getCoachId().equals(coach.getCoachId())) {
            throw new AuthenticationException("Member và Coach không liên kết");
        }

        boolean senderIsCoach;
        if (coach.getUser().getUserId().equals(senderUserId)) {
            senderIsCoach = true;
        } else if (member.getUser().getUserId().equals(senderUserId)) {
            senderIsCoach = false;
        } else {
            throw new AuthenticationException("Không có quyền gửi tin nhắn cặp này!");
        }

        ChatMessage chatMessage = modelMapper.map(dto, ChatMessage.class);
        chatMessage.setSentAt(LocalDateTime.now());
        chatMessage.setSenderIsCoach(senderIsCoach);
        chatMessage.setMember(member);
        chatMessage.setCoach(coach);

        chatMessage = chatMessageRepository.save(chatMessage);

        ChatMessageDTO outDto = modelMapper.map(chatMessage, ChatMessageDTO.class);
        outDto.setSenderName(senderIsCoach ? coach.getUser().getUsername() : member.getUser().getUsername());

        messagingTemplate.convertAndSend("/topic/chat/" + member.getMemberId() + "_" + coach.getCoachId(), outDto);

        return outDto;
    }

    // Lấy lịch sử chat
    public List<ChatMessageDTO> getChatHistory(Long memberId, Long coachId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException("Member not found"));
        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new AuthenticationException("Coach not found"));

        if (!member.getCoach().getCoachId().equals(coach.getCoachId())) {
            throw new AuthenticationException("Không đúng cặp chat!");
        }

        List<ChatMessage> messages = chatMessageRepository.findByMemberAndCoachOrderBySentAtAsc(member, coach);
        return messages.stream()
                .map(msg -> {
                    ChatMessageDTO dto = modelMapper.map(msg, ChatMessageDTO.class);
                    dto.setSenderName(msg.isSenderIsCoach() ? coach.getUser().getUsername() : member.getUser().getUsername());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
