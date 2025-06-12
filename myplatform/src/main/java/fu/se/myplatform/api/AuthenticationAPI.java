package fu.se.myplatform.api;
import fu.se.myplatform.exception.exception.AuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import fu.se.myplatform.dto.AccountResponse;
import fu.se.myplatform.dto.LoginRequest;
import fu.se.myplatform.dto.UpdateProfile;
import fu.se.myplatform.dto.ViewProfile;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.repository.AuthenticationRepository;
import fu.se.myplatform.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthenticationAPI {

    @Autowired
    AuthenticationService authenticationService;

    // api > service > repository

    @PostMapping("/api/register")
    public ResponseEntity register(@RequestBody Account account){
        Account newAccount = authenticationService.register(account);
        return ResponseEntity.ok(newAccount);
    }

    @PostMapping("/api/login")
    public ResponseEntity login(@RequestBody LoginRequest loginRequest){

        AccountResponse account = authenticationService.login(loginRequest);

        return ResponseEntity.ok(account);
    }
    @GetMapping("/api/account/{userName}/profile")
    public ResponseEntity<ViewProfile> viewProfile(
            @Parameter(description = "ID of user to view profile",required = true)
            @PathVariable String userName
    ) {
        try {
            ViewProfile profile = authenticationService.getUserProfile(userName);

            if (profile == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            return ResponseEntity.ok(profile);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/api/account/{userName}/profile")
    public ResponseEntity<Account> updateProfile(
            @PathVariable String userName,
            @RequestBody UpdateProfile updateProfile) {

        try {
            //  Không kiểm tra token hoặc authentication nữa
            Account updatedAccount = authenticationService.updateProfile(userName, updateProfile);
            return ResponseEntity.ok(updatedAccount);
        } catch (Exception e) {
            throw new RuntimeException("Error updating profile", e);
        }
    }

}
