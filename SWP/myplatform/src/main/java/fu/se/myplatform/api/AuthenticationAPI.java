package fu.se.myplatform.api;

import fu.se.myplatform.dto.AccountResponse;
import fu.se.myplatform.dto.LoginRequest;
import fu.se.myplatform.dto.ProfileRequest;
import fu.se.myplatform.dto.ProfileResponse;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController()
@RequestMapping("/api")
public class AuthenticationAPI {

    @Autowired
    AuthenticationService authenticationService;

    // api > service > repository

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody Account account){
        Account newAccount = authenticationService.register(account);
        return ResponseEntity.ok(newAccount);
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginRequest loginRequest){

        AccountResponse account = authenticationService.login(loginRequest);

        return ResponseEntity.ok(account);
    }
    @GetMapping("/account/{username}/profile")
    @Operation(
        summary = "View profile",
        description = "Get profile information for a user",
        security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ProfileResponse> viewProfile(
            @PathVariable String username) {
        ProfileResponse profile = authenticationService.viewProfile(username);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/account/{username}/profile")
    @Operation(
        summary = "Update profile",
        description = "Update profile information for a user",
        security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable String username,
            @RequestBody ProfileRequest profileRequest) {
        ProfileResponse updatedProfile = authenticationService.updateProfile(username, profileRequest);
        return ResponseEntity.ok(updatedProfile);
    }
}
