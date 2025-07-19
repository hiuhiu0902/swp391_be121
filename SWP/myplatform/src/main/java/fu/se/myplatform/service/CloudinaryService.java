package fu.se.myplatform.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private void validateImageType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh định dạng: JPEG, PNG, GIF, WEBP");
        }
    }

    public String uploadImage(MultipartFile file, String folder) throws IOException {
        return uploadImage(file, folder, null);
    }

    public String uploadImage(MultipartFile file, String folder, Transformation transformation) throws IOException {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File không được để trống");
            }

            validateImageType(file);

            Map<String, Object> params = new HashMap<>();
            params.put("folder", folder);
            params.put("resource_type", "auto");

            if (transformation != null) {
                params.put("transformation", transformation);
            }

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new IOException("Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    public String uploadAvatar(MultipartFile file) throws IOException {
        return uploadImage(file, "avatars", new Transformation().width(400).height(400).crop("fill"));
    }

    public String uploadBlogImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh định dạng: JPEG, PNG, GIF, WEBP");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("folder", "blogs");
        params.put("resource_type", "auto");

        // Không giới hạn kích thước cụ thể cho blog image
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
        return (String) uploadResult.get("secure_url");
    }
}
