package in.shubhamprakash681.auth_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        lenient().when(cloudinary.uploader()).thenReturn(uploader);
        cloudinaryService = new CloudinaryService(cloudinary, "tradex/users");
    }

    @Test
    void uploadAvatar_successfulUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", "image/jpeg", "fake-image-bytes".getBytes()
        );

        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(Map.of(
                "secure_url", "https://res.cloudinary.com/demo/image/upload/tradex/users/shubham/avatar.png"
        ));

        String url = cloudinaryService.uploadAvatar("shubham", file);

        assertThat(url).isEqualTo("https://res.cloudinary.com/demo/image/upload/tradex/users/shubham/avatar.png");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(uploader).upload(eq(file.getBytes()), captor.capture());

        Map<String, Object> params = captor.getValue();
        assertThat(params).containsEntry("folder", "tradex/users/shubham");
        assertThat(params).containsEntry("asset_folder", "tradex/users/shubham");
        assertThat(params).containsEntry("public_id", "avatar");
        assertThat(params).containsEntry("format", "png");
        assertThat(params).containsEntry("overwrite", true);
        assertThat(params).containsEntry("invalidate", true);
        assertThat(params).containsEntry("transformation", "c_fill,w_256,h_256,g_face");
    }

    @Test
    void uploadAvatar_rejectsInvalidFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.png", "image/png", new byte[0]);
        assertThatThrownBy(() -> cloudinaryService.uploadAvatar("shubham", emptyFile))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("File is required");

        MockMultipartFile textFile = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());
        assertThatThrownBy(() -> cloudinaryService.uploadAvatar("shubham", textFile))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only image files are allowed");
    }

    @Test
    void uploadAvatar_translatesCloudinaryExceptionToBadGateway() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", "img".getBytes()
        );

        when(uploader.upload(any(byte[].class), anyMap()))
                .thenThrow(new RuntimeException("Cloudinary API error"));

        assertThatThrownBy(() -> cloudinaryService.uploadAvatar("shubham", file))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> {
                    ResponseStatusException rse = (ResponseStatusException) e;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
                    assertThat(rse.getReason()).contains("Failed to upload avatar: Cloudinary API error");
                });
    }

    @Test
    void deleteAvatar_callsDestroyWithExpectedPublicId() throws Exception {
        cloudinaryService.deleteAvatar("shubham");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(uploader).destroy(eq("tradex/users/shubham/avatar"), captor.capture());

        Map<String, Object> params = captor.getValue();
        assertThat(params).containsEntry("invalidate", true);
        assertThat(params).containsEntry("resource_type", "image");
    }

    @Test
    void configurableUsersFolder() {
        CloudinaryService customService = new CloudinaryService(cloudinary, "custom-app/profiles/");
        assertThat(customService.getUserAvatarFolder("john.doe"))
                .isEqualTo("custom-app/profiles/john.doe");
        assertThat(customService.getUserAvatarPublicId("john.doe"))
                .isEqualTo("custom-app/profiles/john.doe/avatar");
    }
}
