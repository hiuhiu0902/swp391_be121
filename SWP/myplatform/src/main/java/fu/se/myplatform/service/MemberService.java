package fu.se.myplatform.service;

import fu.se.myplatform.dto.CoachShortDTO;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.Coach;
import fu.se.myplatform.entity.Member;
import fu.se.myplatform.exception.exception.AuthenticationException;
import fu.se.myplatform.repository.CoachRepository;
import fu.se.myplatform.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {
    @Autowired
    CoachRepository coachRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    CoachService coachService;

    // 1. Upload avatar image for member
    public Member updateProfileImage(Long memberId, MultipartFile file) throws IOException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException("Member not found"));
        member.setProfileImage(file.getBytes());
        return memberRepository.save(member);
    }

    // 6. Get member's avatar as Base64 string
    public String getProfileImageBase64(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException("Member not found"));
        byte[] imageData = member.getProfileImage();
        return imageData != null ? Base64.getEncoder().encodeToString(imageData) : null;
    }

    // 2. Assign a coach to member (with coach capacity check)
    public Member assignCoach(Account account, Long coachId) {
        // Tìm member theo account
        Member member = memberRepository.findByUser(account);
        if (member == null) {
            throw new AuthenticationException("Bạn không phải là member!");
        }
        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new AuthenticationException("Coach not found"));
        // Check capacity
        if (!coachService.hasCapacity(coachId)) {
            throw new AuthenticationException("Coach đã đủ số lượng member");
        }
        member.setCoach(coach);
        return memberRepository.save(member);
    }
    public List<CoachShortDTO> getAvailableCoach() {
        // Lấy tất cả các coach từ database
        List<Coach> coaches = coachRepository.findAll();

        // Lọc coach còn capacity và chuyển đổi thành CoachShortDTO
        return coaches.stream()
                .filter(coach -> coachService.hasCapacity(coach.getCoachId()))
                .map(coach -> {
                    CoachShortDTO dto = new CoachShortDTO();
                    dto.setId(coach.getCoachId());
                    dto.setName(coach.getUser().getFullName()); // Hoặc lấy full name nếu cần
                    // Nếu có avatar URL, trả về link
                    dto.setAvatarUrl(coach.getProfileImage() != null ? "/avatars/" + coach.getCoachId() + ".jpg" : null);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 5. Update VIP status for member (set or remove VIP and dates)
    public Member updateVipStatus(Long memberId, boolean isVip, LocalDate startDate, LocalDate endDate) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException("Member not found"));
        member.setIsVip(isVip);
        if (isVip) {
            member.setVipStartDate(startDate != null ? startDate : LocalDate.now());
            member.setVipExpiryDate(endDate);
        } else {
            member.setVipStartDate(null);
            member.setVipExpiryDate(null);
        }
        return memberRepository.save(member);
    }
    public Member getMemberProfile(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException("Member not found"));
    }
}
