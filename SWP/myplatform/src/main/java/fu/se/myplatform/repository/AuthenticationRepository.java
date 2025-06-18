package fu.se.myplatform.repository;

import fu.se.myplatform.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthenticationRepository extends JpaRepository<Account, Long> {
    Account findAccountByUserName(String userName);
}
