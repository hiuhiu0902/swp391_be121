package fu.se.myplatform.service;

import fu.se.myplatform.entity.Coach;
import fu.se.myplatform.repository.CoachRepository;
import fu.se.myplatform.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class CoachService {
    @Autowired
    CoachRepository coachRepository;
    @Autowired
    MemberRepository memberRepository;

    public Coach findById(Long coachId) {
        return coachRepository.findById(coachId)
                .orElseThrow(() -> new RuntimeException("Coach not found"));
    }

    public boolean hasCapacity(Long coachId) {
        // Count how many members are currently assigned to this coach
        long count = memberRepository.countByCoachId(coachId);
        return count < 5;
    }
}
