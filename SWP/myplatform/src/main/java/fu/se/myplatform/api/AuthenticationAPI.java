package fu.se.myplatform.api;

import fu.se.myplatform.dto.*;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
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

<<<<<<< HEAD
    @PostMapping("/appi/forgot-password")
    public ResponseEntity forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest){
        authenticationService.forgotPassword(forgotPasswordRequest);
        return ResponseEntity.ok("Forgot Password successful");
    }

    @SecurityRequirement(
            name = "api"
    )
    @PostMapping("/api/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest){
        authenticationService.resetPassword(resetPasswordRequest);
        ResponseEntity.ok("Reset Password successful");
    }
=======
>>>>>>> 4a1f5c0c39b43b62e434892413b2abeb7b8f2c9c
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
