package in.shubhamprakash681.auth_service.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import in.shubhamprakash681.auth_service.dtos.AuthDtos.UserResponse;
import in.shubhamprakash681.auth_service.dtos.UserDtos.ChangePasswordRequest;
import in.shubhamprakash681.auth_service.dtos.UserDtos.UpdateProfileRequest;
import in.shubhamprakash681.auth_service.entity.User;
import in.shubhamprakash681.auth_service.repositories.UserRepository;
import in.shubhamprakash681.common_lib.security.JwtPrincipal;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public UserResponse me(JwtPrincipal parsedToken) {
        return authService.toResponse(findUser(parsedToken));
    }

    @Transactional
    public UserResponse updateProfile(JwtPrincipal parsedToken, UpdateProfileRequest updateProfileRequest) {
        User user = findUser(parsedToken);

        user.setFullName(updateProfileRequest.fullName().trim());
        return authService.toResponse(user);
    }

    @Transactional
    public void changePassword(JwtPrincipal parsedToken, ChangePasswordRequest request) {
        User user = findUser(parsedToken);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    private User findUser(JwtPrincipal parsedToken) {
        return userRepository.findById(parsedToken.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found"));
    }
}
