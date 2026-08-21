package in.shubhamprakash681.auth_service.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Set;

import in.shubhamprakash681.auth_service.entity.OtpToken;
import in.shubhamprakash681.auth_service.repositories.OtpTokenRepository;
import in.shubhamprakash681.common_lib.email.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import in.shubhamprakash681.auth_service.dtos.AuthDtos.AuthResponse;
import in.shubhamprakash681.auth_service.dtos.AuthDtos.LoginRequest;
import in.shubhamprakash681.auth_service.dtos.AuthDtos.SignupRequest;
import in.shubhamprakash681.auth_service.dtos.AuthDtos.UserResponse;
import in.shubhamprakash681.auth_service.entity.RevokedToken;
import in.shubhamprakash681.auth_service.entity.User;
import in.shubhamprakash681.auth_service.repositories.RevokedTokenRepository;
import in.shubhamprakash681.auth_service.repositories.UserRepository;
import in.shubhamprakash681.common_lib.security.JwtTokenService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;

    @Value("${tradex.email.from:noreply@tradex.com}")
    private String fromEmail;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User userObj = User.builder()
                .email(email)
                .fullName(request.fullName().trim())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        if (userObj == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to register user");
        }

        User user = userRepository.save(userObj);

        return tokensFor(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid Email or Password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid Email or Password");
        }

        return tokensFor(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(String refreshToken) {
        if (refreshToken == null || sha256(refreshToken) == null
                || revokedTokenRepository.existsById(sha256(refreshToken))
                || !jwtTokenService.isRefreshToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        var parsedToken = jwtTokenService.parse(refreshToken);
        User user = userRepository.findById(parsedToken.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        return tokensFor(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        revokedTokenRepository.save(new RevokedToken(sha256(refreshToken)));
    }

    // Helper functions
    private AuthResponse tokensFor(User user) {
        Set<String> roles = Set.copyOf(user.getRoles().stream().sorted().map(Enum::toString).toList());

        return new AuthResponse(
                jwtTokenService.createAccessToken(user.getId(), user.getEmail(), roles),
                jwtTokenService.createRefreshToken(user.getId(), user.getEmail(), roles),
                toResponse(user));
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRoles(),
                user.getCreatedAt());
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : digest) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    // Random 6-digit number generator
    private String generateOtp() {
        return String.format("%06d", new java.util.Random().nextInt(999999));
    }

    @Transactional
    public void requestPasswordRecovery(String email) {
        String lowerEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(lowerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String otp = generateOtp();
        
        OtpToken token = otpTokenRepository.findByEmail(lowerEmail)
                .orElse(OtpToken.builder().email(lowerEmail).build());
                
        token.setOtp(otp);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        
        otpTokenRepository.save(token);

        emailService.sendEmail(
            fromEmail,
            user.getEmail(), 
            "TradeX Password Reset", 
            "Your OTP for password reset is: " + otp + "\nThis OTP is valid for 10 minutes."
        );
    }

    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        String lowerEmail = email.trim().toLowerCase();
        
        OtpToken token = otpTokenRepository.findByEmail(lowerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP"));
        
        if (!token.getOtp().equals(otp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }

        if (token.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            otpTokenRepository.delete(token);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP has expired");
        }

        User user = userRepository.findByEmailIgnoreCase(lowerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        otpTokenRepository.delete(token);
    }
}
