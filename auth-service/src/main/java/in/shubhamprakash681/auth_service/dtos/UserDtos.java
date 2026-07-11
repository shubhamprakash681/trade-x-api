package in.shubhamprakash681.auth_service.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserDtos() {
    public record UpdateProfileRequest(@NotBlank @Size(min = 2, max = 120) String fullName) {
    }

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8, max = 80) String newPassword) {
    }
}
