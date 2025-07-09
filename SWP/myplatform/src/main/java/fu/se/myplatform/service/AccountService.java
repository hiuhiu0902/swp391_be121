package fu.se.myplatform.service;

import fu.se.myplatform.entity.Account;
import fu.se.myplatform.repository.AccountRepository;
import fu.se.myplatform.repository.AuthenticationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService{
    @Autowired
    private AuthenticationRepository authenticationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Account> getAllAccounts() {
        return authenticationRepository.findAll();
    }

//    @PreAuthorize("hasAuthority('ADMIN')")
//    public Account updateRole(Long accountId, String newRole) {
//        Account account = authenticationRepository.findById(accountId)
//                .orElseThrow(() -> new RuntimeException("Account not found"));
//        account.setRole(newRole);
//        return authenticationRepository.save(account);
//    }
// 4. Block a user account

}
