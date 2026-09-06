package in.shubhamprakash681.auth_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private static final String AVATAR_FOLDER = "tradex/avatars";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    private final Cloudinary cloudinary;

    /**
     * Uploads an avatar image to Cloudinary.
     * Uses the userId as the public_id so re-uploads overwrite the previous avatar.
     *
     * @return the secure URL of the uploaded image
     */
    public String uploadAvatar(Long userId, MultipartFile file) {
        validateFile(file);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", String.valueOf(userId),
                    "folder", AVATAR_FOLDER,
                    "overwrite", true,
                    "resource_type", "image",
                    "transformation", "c_fill,w_256,h_256,g_face,q_auto,f_auto"
            ));

            return (String) result.get("secure_url");
        } catch (IOException e) {
            log.error("Failed to upload avatar for user {}", userId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload avatar");
        }
    }

    /**
     * Deletes the avatar image from Cloudinary.
     */
    public void deleteAvatar(Long userId) {
        try {
            cloudinary.uploader().destroy(AVATAR_FOLDER + "/" + userId, ObjectUtils.asMap(
                    "resource_type", "image"
            ));
        } catch (IOException e) {
            log.warn("Failed to delete avatar for user {}", userId, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size must be less than 5 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed");
        }
    }
}
