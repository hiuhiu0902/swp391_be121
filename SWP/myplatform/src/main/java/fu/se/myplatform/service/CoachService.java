package fu.se.myplatform.service;

import fu.se.myplatform.dto.MemberShortDTO;
import fu.se.myplatform.entity.Coach;
import fu.se.myplatform.entity.Member;
import fu.se.myplatform.repository.CoachRepository;
import fu.se.myplatform.repository.MemberRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CoachService {
    @Autowired
    CoachRepository coachRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    private ModelMapper modelMapper;

    public Coach findById(Long coachId) {
        return coachRepository.findById(coachId)
                .orElseThrow(() -> new RuntimeException("Coach not found"));
    }

    public boolean hasCapacity(Long coachId) {
        // Count how many members are currently assigned to this coach
        long count = memberRepository.countByCoachId(coachId);
        return count < 5;
    }
    public List<MemberShortDTO> getMembersAssignedToCoach(Long coachId) {
        // Lấy Coach từ database theo coachId
        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new RuntimeException("Coach not found"));

        // Lấy danh sách các Member đã được phân cho Coach
        List<Member> members = coach.getMembers();

        // Chuyển đổi từ Member sang MemberShortDTO
        return members.stream()
                .map(member -> {
                    // Ánh xạ từ Member sang MemberShortDTO
                    MemberShortDTO memberShortDTO = new MemberShortDTO();
                    memberShortDTO.setMemberId(member.getMemberId());
                    memberShortDTO.setFullName(member.getUser().getFullName());
                    memberShortDTO.setStatus(member.getStatus());
                    return memberShortDTO;
                })
                .collect(Collectors.toList());
    }
}
