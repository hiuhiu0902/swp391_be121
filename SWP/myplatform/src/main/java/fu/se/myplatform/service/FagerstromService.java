package fu.se.myplatform.service;

import fu.se.myplatform.dto.FagerstromTestRequest;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.Assessment;
import fu.se.myplatform.enums.DependencyLevel;
import fu.se.myplatform.repository.AssessmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FagerstromService {
    @Autowired AssessmentRepository assessmentRepository;
    @Autowired AuthenticationService authenticationService;

    public Assessment performAndSaveAssessment(FagerstromTestRequest testRequest) {
        if (testRequest == null) {
            throw new IllegalArgumentException("Fagerstrom test request cannot be null");
        }
        int score = calculateFagerstromScore(testRequest);
        DependencyLevel level = determineDependencyLevel(score);
        Account currentUser = authenticationService.getCurrentAccount();

        Assessment assessment = new Assessment();
        assessment.setScore(score);
        assessment.setDependencyLevel(level);
        assessment.setAccount(currentUser);
        assessment.setCreatedAt(LocalDateTime.now());

        return assessmentRepository.save(assessment);
    }

    private int calculateFagerstromScore(FagerstromTestRequest test) {
        return test.getAnswer1() + test.getAnswer2() + test.getAnswer3() +
                test.getAnswer4() + test.getAnswer5() + test.getAnswer6();
    }

    private DependencyLevel determineDependencyLevel(int score) {
        if (score <= 4) return DependencyLevel.LOW;
        if (score <= 7) return DependencyLevel.MEDIUM;
        return DependencyLevel.HIGH;
    }
}
