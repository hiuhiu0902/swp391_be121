package fu.se.myplatform.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) throws IOException {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File không được để trống");
            }

            Map<String, Object> params = new HashMap<>();
            params.put("folder", "avatars");
            params.put("resource_type", "auto");
            params.put("transformation", new Transformation().width(400).height(400).crop("fill"));

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new IOException("Lỗi khi upload ảnh: " + e.getMessage());
        }
    }
}
