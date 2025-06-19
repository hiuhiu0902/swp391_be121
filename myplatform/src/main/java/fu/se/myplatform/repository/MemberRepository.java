package fu.se.myplatform.repository;

import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByUser(Account user);

    void deleteByUser(Account account);
}

