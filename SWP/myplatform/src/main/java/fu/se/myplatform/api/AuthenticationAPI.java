package fu.se.myplatform.api;

import fu.se.myplatform.dto.*;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//@CrossOrigin(origins = "http://localhost:3000")
@RestController()
@RequestMapping("/api")
public class AuthenticationAPI {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationAPI.class);


    @Autowired
    AuthenticationService authenticationService;

    // api > service > repository

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody Account account){
        logger.info("Registering account for user: {}", account.getUsername());

        Account newAccount = authenticationService.register(account);
        logger.info("Account registered successfully for user: {}", newAccount.getUsername());

        return ResponseEntity.ok("Register Successfully");
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

    @PostMapping("/forgot-password")
    public ResponseEntity forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest){
        authenticationService.forgotPassword(forgotPasswordRequest);
        return ResponseEntity.ok("Forgot Password successful");
    }

    @SecurityRequirement(
            name = "api"
    )
    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest){
        authenticationService.resetPassword(resetPasswordRequest);
        ResponseEntity.ok("Reset Password successful");
    }

}
