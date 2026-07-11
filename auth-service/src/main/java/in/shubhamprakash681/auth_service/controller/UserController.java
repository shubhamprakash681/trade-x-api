package in.shubhamprakash681.auth_service.controller;


import in.shubhamprakash681.auth_service.dtos.AuthDtos;
import in.shubhamprakash681.auth_service.dtos.UserDtos;
import in.shubhamprakash681.auth_service.service.UserService;
import in.shubhamprakash681.common_lib.security.JwtPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    AuthDtos.UserResponse me(@AuthenticationPrincipal JwtPrincipal token) {
        return userService.me(token);
    }

    @PutMapping("/me")
    AuthDtos.UserResponse updateProfile(@AuthenticationPrincipal JwtPrincipal token,
                               @Valid @RequestBody UserDtos.UpdateProfileRequest request) {
        return userService.updateProfile(token, request);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void changePassword(@AuthenticationPrincipal JwtPrincipal token,
                        @Valid @RequestBody UserDtos.ChangePasswordRequest request) {
        userService.changePassword(token, request);
    }
}
