package fu.se.myplatform.api;

import fu.se.myplatform.exception.UserNotFoundException;
import fu.se.myplatform.service.CloudinaryService;
import fu.se.myplatform.entity.Account;
import fu.se.myplatform.repository.AccountRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/avatar")
@CrossOrigin
@SecurityRequirement(name = "api")
@Tag(name = "Avatar", description = "API quản lý avatar của user")
public class AvatarAPI {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private AccountRepository accountRepository;

    @Operation(summary = "Upload avatar mới",
            description = "Upload file ảnh avatar mới cho user hiện tại. Ảnh sẽ được lưu trên Cloudinary.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Upload thành công",
                    content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = UploadResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Lỗi validation hoặc upload",
                    content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))})
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatar(
        @Parameter(description = "File ảnh avatar (jpg, png, etc.)", required = true)
        @RequestParam("file") MultipartFile file,
        Authentication authentication) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File không được để trống"));
            }

            // Get current user safely
            Account account = accountRepository.findByUserName(authentication.getName());
            if (account == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy người dùng"));
            }

            // Upload to cloudinary with specific options
            String imageUrl = cloudinaryService.uploadAvatar(file);

            // Update user avatar URL
            account.setAvatarUrl(imageUrl);
            accountRepository.save(account);

            return ResponseEntity.ok(Map.of("url", imageUrl));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Lỗi khi upload avatar: " + e.getMessage()));
        }
    }

    @Operation(summary = "Lấy avatar hiện tại",
            description = "Lấy URL avatar của user hiện tại")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy thành công",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"avatarUrl\":\"https://cloudinary.com/...\"}"))),
        @ApiResponse(responseCode = "400", description = "Lỗi khi lấy avatar",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"error\":\"Lỗi khi lấy avatar\"}")))
    })
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentAvatar(Authentication authentication) {
        try {
            Account account = (Account) authentication.getPrincipal();
            String avatarUrl = account.getAvatarUrl();

            if (avatarUrl == null) {
                return ResponseEntity.ok().body(Map.of(
                    "avatarUrl", ""  // Hoặc URL mặc định nếu bạn muốn
                ));
            }

            return ResponseEntity.ok().body(Map.of(
                "avatarUrl", avatarUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi khi lấy avatar: " + e.getMessage()
            ));
        }
    }

    @Schema(name = "UploadResponse")
    private static class UploadResponse {
        @Schema(example = "Upload avatar thành công")
        public String message;
        @Schema(example = "https://cloudinary.com/...")
        public String avatarUrl;
    }

    @Schema(name = "ErrorResponse")
    private static class ErrorResponse {
        @Schema(example = "File phải là hình ảnh")
        public String error;
    }
}
