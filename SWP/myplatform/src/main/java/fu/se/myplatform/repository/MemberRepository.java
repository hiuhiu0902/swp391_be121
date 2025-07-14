package fu.se.myplatform.repository;

import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.Member;
import fu.se.myplatform.entity.Coach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByUser(Account user);

    Member findByUser_UserId(Long userId);

    void deleteByUser(Account account);

    List<Member> findAllByCoach(Coach coach);

    @Query("SELECT COUNT(m) FROM Member m WHERE m.coach.coachId = :coachId")
    long countByCoachId(Long coachId);
}
