package fu.se.myplatform.repository;

import fu.se.myplatform.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Finds an account by its username.
     *
     * @param username the username of the account
     * @return the account with the specified username, or null if not found
     */

    /**
     * Finds an account by its email.
     *
     * @param email the email of the account
     * @return the account with the specified email, or null if not found
     */
    Account findByEmail(String email);
    Account findByUserName(String userName);
}
