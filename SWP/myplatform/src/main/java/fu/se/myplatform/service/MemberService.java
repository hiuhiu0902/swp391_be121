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
import org.springframework.transaction.annotation.Transactional;
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

    // --- LOGIC MỚI ĐỂ NÂNG CẤP VIP ---
    private static final int VIP_DURATION_DAYS = 30;

    /**
     * Phương thức công khai để nâng cấp VIP cho một member.
     * Tự động xử lý việc gia hạn nếu member đã là VIP.
     */
    @Transactional
    public Member upgradeMemberToVip(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException("Member not found"));

        LocalDate newExpiryDate;

        // Nếu member đã là VIP và chưa hết hạn, thì cộng dồn ngày
        if (member.getIsVip() && member.getVipExpiryDate() != null && member.getVipExpiryDate().isAfter(LocalDate.now())) {
            newExpiryDate = member.getVipExpiryDate().plusDays(VIP_DURATION_DAYS);
        } else {
            // Nếu là VIP lần đầu hoặc VIP đã hết hạn, thì tính từ ngày hôm nay
            newExpiryDate = LocalDate.now().plusDays(VIP_DURATION_DAYS);
        }

        // Cập nhật trạng thái VIP
        updateVipDetails(member, true, LocalDate.now(), newExpiryDate);

        return memberRepository.save(member);
    }

    /**
     * Phương thức riêng tư để cập nhật các trường liên quan đến VIP.
     * Giúp đóng gói logic và tránh bị gọi sai từ bên ngoài.
     */
    private void updateVipDetails(Member member, boolean isVip, LocalDate startDate, LocalDate endDate) {
        member.setIsVip(isVip);
        Account accountMember = member.getUser();
        if (isVip) {
            // Chỉ cập nhật ngày bắt đầu nếu trước đó chưa phải là VIP hoặc VIP đã hết hạn
            if (member.getVipStartDate() == null || member.getVipExpiryDate().isBefore(LocalDate.now())) {
                member.setVipStartDate(startDate);
            }
            member.setVipExpiryDate(endDate);
        } else {
            member.setVipStartDate(null);
            member.setVipExpiryDate(null);
        }
    }

    // --- CÁC PHƯƠNG THỨC KHÁC GIỮ NGUYÊN ---
    // ... (updateProfileImage, assignCoach, v.v...)

    public Member updateProfileImage(Long memberId, MultipartFile file) throws IOException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException("Member not found"));
        member.setProfileImage(file.getBytes());
        return memberRepository.save(member);
    }

    public String getProfileImageBase64(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException("Member not found"));
        byte[] imageData = member.getProfileImage();
        return imageData != null ? Base64.getEncoder().encodeToString(imageData) : null;
    }

    public Member assignCoach(Account account, Long coachId) {
        Member member = memberRepository.findByUser(account);
        if(member.getIsVip().equals(false)){
            throw new AuthenticationException("Member must be VIP to assign a coach");
        }else {
            Coach coach = coachRepository.findById(coachId)
                    .orElseThrow(() -> new AuthenticationException("Coach not found"));
            if (!coachService.hasCapacity(coachId)) {
                throw new AuthenticationException("Coach đã đủ số lượng member");
            }
            member.setCoach(coach);
        }
        return memberRepository.save(member);
    }

    public List<CoachShortDTO> getAvailableCoach() {
        List<Coach> coaches = coachRepository.findAll();
        return coaches.stream()
                .filter(coach -> coachService.hasCapacity(coach.getCoachId()))
                .map(coach -> {
                    CoachShortDTO dto = new CoachShortDTO();
                    dto.setId(coach.getCoachId());
                    dto.setName(coach.getUser().getFullName());
                    dto.setAvatarUrl(coach.getUser().getAvatarUrl());
                    dto.setStatus(coach.getStatus());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public Member getMemberProfile(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException("Member not found"));
    }

    public CoachShortDTO getAssignedCoach(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationException("Member not found"));
        Coach coach = member.getCoach();
        if (coach == null) {
            throw new AuthenticationException("No coach assigned to this member");
        }
        CoachShortDTO coachShortDTO = new CoachShortDTO();
        coachShortDTO.setId(coach.getCoachId());
        coachShortDTO.setName(coach.getUser().getFullName());
        coachShortDTO.setAvatarUrl(coach.getProfileImage() != null
                ? "/avatars/" + coach.getCoachId() + ".jpg" : null);
        return coachShortDTO;
    }
}