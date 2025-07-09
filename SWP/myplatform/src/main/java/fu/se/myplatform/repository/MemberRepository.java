package fu.se.myplatform.repository;

import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByUser(Account user);

    void deleteByUser(Account account);
    @Query("SELECT COUNT(m) FROM Member m WHERE m.coach.id = :coachId")
    long countByCoachId(Long coachId);

    // Có thể thêm tìm kiếm member theo account nếu cần
    Member findByAccountId(Long accountId);
}

