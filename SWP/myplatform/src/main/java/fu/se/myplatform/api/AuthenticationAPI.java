package fu.se.myplatform.api;

import fu.se.myplatform.dto.AccountResponse;
import fu.se.myplatform.dto.CreateAccountRequest;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @PostMapping("/admin/create-account")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> createSpecialAccount(@RequestBody CreateAccountRequest request) {
        AccountResponse newAccount = authenticationService.createSpecialAccount(request);
        return ResponseEntity.ok(newAccount);
    }
    @PutMapping("/admin/update-account/{userId}")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> updateAccountByAdmin(
            @PathVariable Long userId,
            @RequestBody fu.se.myplatform.dto.UpdateAccountRequest request) {
        AccountResponse updated = authenticationService.updateAccountByAdmin(userId, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/admin/accounts")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        List<AccountResponse> accounts = authenticationService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }


    @GetMapping("/account/{userId}/detail")
    @Operation(summary = "View account detail", description = "Get detail information for an account, including createdAt")
    public ResponseEntity<AccountResponse> getAccountDetail(@PathVariable Long userId) {
        AccountResponse account = authenticationService.getAccountDetail(userId);
        return ResponseEntity.ok(account);
    }
    @DeleteMapping("/admin/account/{userId}")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long userId) {
        authenticationService.deleteAccount(userId);
        return ResponseEntity.noContent().build();
    }
//   Phần upimage code mai~ ma' test như cục cứt chán thật
//    @PostMapping("/account/{username}/profile/image")
//    @Operation(
//        summary = "Upload profile image",
//        description = "Upload a profile image for a user",
//        security = {@SecurityRequirement(name = "bearerAuth")}
//    )
//    @SecurityRequirement(name = "bearerAuth")
//    public ResponseEntity<String> uploadProfileImage(
//            @PathVariable String username,
//            @RequestParam("image") MultipartFile imageFile) {
//        String imageUrl = authenticationService.uploadProfileImage(username, imageFile);
//        return ResponseEntity.ok(imageUrl);
//    }
}
