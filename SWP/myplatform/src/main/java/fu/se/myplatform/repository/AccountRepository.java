package fu.se.myplatform.repository;

import fu.se.myplatform.entity.Account;
import fu.se.myplatform.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

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

    /**
     * Finds all accounts with a specific role.
     *
     * @param role the role to search for
     * @return list of accounts with the specified role
     */
    List<Account> findByRole(Role role);
}
