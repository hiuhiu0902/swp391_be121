package fu.se.myplatform.repository;

import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.Coach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoachRepository extends JpaRepository<Coach, Long> {
    Coach findByUser(Account user);

    void deleteByUser(Account account);

    List<Coach> findAllByUserIn(List<Account> accounts);
}
