package in.shubhamprakash681.auth_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class CloudinaryService {
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    private final Cloudinary cloudinary;
    private final String usersFolder;

    public CloudinaryService(
            Cloudinary cloudinary,
            @Value("${tradex.cloudinary.users-folder:${tradex.cloudinary.folder:tradex/users}}") String usersFolder
    ) {
        this.cloudinary = cloudinary;
        this.usersFolder = normalizeFolder(usersFolder);
    }

    /**
     * Uploads an avatar image to Cloudinary under [usersFolder]/[username]/avatar.png.
     * Overwrites previous avatar and invalidates CDN cache.
     *
     * @param username the username or user identifier
     * @param file     the image file
     * @return the secure URL of the uploaded image
     */
    public String uploadAvatar(String username, MultipartFile file) {
        validateFile(file);

        String folder = getUserAvatarFolder(username);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "asset_folder", folder,
                    "public_id", "avatar",
                    "overwrite", true,
                    "invalidate", true,
                    "resource_type", "image",
                    "format", "png",
                    "transformation", "c_fill,w_256,h_256,g_face"
            ));

            return (String) result.get("secure_url");
        } catch (Exception e) {
            log.error("Failed to upload avatar for user {}", username, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to upload avatar: " + e.getMessage(), e);
        }
    }

    public String uploadAvatar(Long userId, MultipartFile file) {
        return uploadAvatar(String.valueOf(userId), file);
    }

    /**
     * Deletes the avatar image from Cloudinary under [usersFolder]/[username]/avatar.
     *
     * @param username the username or user identifier
     */
    public void deleteAvatar(String username) {
        String publicId = getUserAvatarPublicId(username);
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                    "invalidate", true,
                    "resource_type", "image"
            ));
        } catch (Exception e) {
            log.warn("Failed to delete avatar for user {}", username, e);
        }
    }

    public void deleteAvatar(Long userId) {
        deleteAvatar(String.valueOf(userId));
    }

    public String getUserAvatarFolder(String username) {
        return usersFolder + "/" + sanitizeUsername(username);
    }

    public String getUserAvatarPublicId(String username) {
        return getUserAvatarFolder(username) + "/avatar";
    }

    private String normalizeFolder(String folder) {
        if (folder == null || folder.isBlank()) {
            return "tradex/users";
        }
        return folder.trim().replaceAll("^/+|/+$", "");
    }

    private String sanitizeUsername(String username) {
        if (username == null || username.isBlank()) {
            return "user";
        }
        return username.trim().toLowerCase().replaceAll("[^a-zA-Z0-9._-]", "_");
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
