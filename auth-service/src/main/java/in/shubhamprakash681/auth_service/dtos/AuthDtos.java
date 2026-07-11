package in.shubhamprakash681.auth_service.dtos;

import java.time.LocalDateTime;
import java.util.Set;

import in.shubhamprakash681.auth_service.enums.UserRoles;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthDtos() {
    public record SignupRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 2, max = 120) String fullName,
            @NotBlank @Size(min = 8, max = 80) String password) {
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    public record LogoutRequest(@NotBlank String refreshToken) {
    }

    public record AuthResponse(String accessToken, String refreshToken, UserResponse user) {
    }

    public record UserResponse(Long id, String email, String fullName, Set<UserRoles> roles, LocalDateTime createdAt) {

    }

}
