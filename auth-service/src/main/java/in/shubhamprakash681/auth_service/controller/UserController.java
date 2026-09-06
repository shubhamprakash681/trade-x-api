package in.shubhamprakash681.auth_service.controller;


import in.shubhamprakash681.auth_service.dtos.AuthDtos;
import in.shubhamprakash681.auth_service.dtos.UserDtos;
import in.shubhamprakash681.auth_service.service.UserService;
import in.shubhamprakash681.common_lib.security.JwtPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    AuthDtos.UserResponse uploadAvatar(@AuthenticationPrincipal JwtPrincipal token,
                                       @RequestParam("file") MultipartFile file) {
        return userService.uploadAvatar(token, file);
    }

    @DeleteMapping("/me/avatar")
    AuthDtos.UserResponse deleteAvatar(@AuthenticationPrincipal JwtPrincipal token) {
        return userService.deleteAvatar(token);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void changePassword(@AuthenticationPrincipal JwtPrincipal token,
                        @Valid @RequestBody UserDtos.ChangePasswordRequest request) {
        userService.changePassword(token, request);
    }
}

