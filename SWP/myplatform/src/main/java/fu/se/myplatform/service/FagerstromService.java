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

        Assessment assessment = assessmentRepository.findByAccount(currentUser);
        if(assessment == null) {
            Assessment newAssessment = new Assessment();
            newAssessment.setScore(score);
            newAssessment.setDependencyLevel(level);
            newAssessment.setAccount(currentUser);
            newAssessment.setCreatedAt(LocalDateTime.now());
            assessmentRepository.save(newAssessment);
            return newAssessment;
        }else{
            assessment.setScore(score);
            assessment.setDependencyLevel(level);
            assessment.setAccount(currentUser);
            assessment.setCreatedAt(LocalDateTime.now());
            assessmentRepository.save(assessment);
            return  assessment;
        }
    }

    public boolean hasAssessment(Long userId) {
        return assessmentRepository.existsByAccount_UserId(userId);
    }
    public Assessment getAssessment() {
        Account currentUser = authenticationService.getCurrentAccount();
        Assessment assessment = assessmentRepository.findByAccount(currentUser);
        return assessment;
    }
    private int calculateFagerstromScore(FagerstromTestRequest test) {
        return test.getAnswer1() + test.getAnswer2() + test.getAnswer3() +
                test.getAnswer4() + test.getAnswer5() + test.getAnswer6();
    }

    public DependencyLevel determineDependencyLevel(int score) {
        if (score <= 8) return DependencyLevel.LOW;
        if (score <= 15) return DependencyLevel.MEDIUM;
        return DependencyLevel.HIGH;
    }
}
