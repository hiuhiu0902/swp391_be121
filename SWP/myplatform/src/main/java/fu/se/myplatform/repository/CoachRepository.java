package fu.se.myplatform.repository;

import fu.se.myplatform.entity.Account;
import fu.se.myplatform.entity.Coach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoachRepository extends JpaRepository<Coach, Long> {
    Coach findByUser(Account user);

    void deleteByUser(Account account);
    Optional<Coach> findById(Long coachId);


    List<Coach> findAllByUserIn(List<Account> accounts);
}
