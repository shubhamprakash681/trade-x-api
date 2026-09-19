package in.shubhamprakash681.auth_service.service;

import in.shubhamprakash681.auth_service.dtos.AuthDtos.UserResponse;
import in.shubhamprakash681.auth_service.entity.User;
import in.shubhamprakash681.auth_service.repositories.UserRepository;
import in.shubhamprakash681.common_lib.security.JwtPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthService authService;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private UserService userService;

    @Test
    void getUsername_extractsLocalPartOfEmail() {
        User user = User.builder().id(123L).email("shubhamprakash681@gmail.com").build();
        assertThat(userService.getUsername(user)).isEqualTo("shubhamprakash681");

        User userWithUpper = User.builder().id(456L).email("John.Doe@Example.com").build();
        assertThat(userService.getUsername(userWithUpper)).isEqualTo("john.doe");

        User fallbackUser = User.builder().id(789L).email("").build();
        assertThat(userService.getUsername(fallbackUser)).isEqualTo("user_789");
    }

    @Test
    void uploadAvatar_usesExtractedUsername() {
        JwtPrincipal principal = new JwtPrincipal(1L, "shubhamprakash681@gmail.com", java.util.List.of("ROLE_USER"));
        User user = User.builder().id(1L).email("shubhamprakash681@gmail.com").build();
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", new byte[]{1, 2, 3});

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cloudinaryService.uploadAvatar("shubhamprakash681", file))
                .thenReturn("https://res.cloudinary.com/test/image/upload/tradex/users/shubhamprakash681/avatar.png");
        when(authService.toResponse(user)).thenReturn(mock(UserResponse.class));

        userService.uploadAvatar(principal, file);

        verify(cloudinaryService).uploadAvatar("shubhamprakash681", file);
        assertThat(user.getAvatarUrl()).isEqualTo("https://res.cloudinary.com/test/image/upload/tradex/users/shubhamprakash681/avatar.png");
    }

    @Test
    void deleteAvatar_usesExtractedUsername() {
        JwtPrincipal principal = new JwtPrincipal(1L, "shubhamprakash681@gmail.com", java.util.List.of("ROLE_USER"));
        User user = User.builder()
                .id(1L)
                .email("shubhamprakash681@gmail.com")
                .avatarUrl("https://res.cloudinary.com/test/image/upload/tradex/users/shubhamprakash681/avatar.png")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(authService.toResponse(user)).thenReturn(mock(UserResponse.class));

        userService.deleteAvatar(principal);

        verify(cloudinaryService).deleteAvatar("shubhamprakash681");
        assertThat(user.getAvatarUrl()).isNull();
    }
}
