package in.shubhamprakash681.common_lib.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class JwtTokenService {
    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
        validate(properties);
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId, String email, Set<String> roles) {
        return createToken(userId, email, roles, Duration.ofMinutes(properties.getAccessTokenMinutes()), "access");
    }

    public String createRefreshToken(Long userId, String email, Set<String> roles) {
        return createToken(userId, email, roles, Duration.ofDays(properties.getRefreshTokenDays()), "refresh");
    }

    public JwtPrincipal parse(String token) {
        Claims claims = parseClaims(token);
        Object rolesValue = claims.get("roles");
        List<String> roles = rolesValue instanceof List<?> values
                ? values.stream().map(String::valueOf).toList()
                : List.of();
        return new JwtPrincipal(Long.valueOf(claims.getSubject()), claims.get("email", String.class), roles);
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(parseClaims(token).get("typ", String.class));
    }

    private String createToken(Long userId, String email, Set<String> roles, Duration ttl, String type) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .claim("email", email)
                .claim("roles", roles)
                .claim("typ", type)
                .signWith(signingKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private void validate(JwtProperties properties) {
        if (isBlank(properties.getIssuer())) {
            throw new IllegalStateException("Missing required configuration: tradex.jwt.issuer");
        }
        if (isBlank(properties.getSecret())) {
            throw new IllegalStateException("Missing required configuration: tradex.jwt.secret");
        }
        if (properties.getSecret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "Configuration tradex.jwt.secret must be at least 32 bytes for HS256 signing");
        }
        if (Objects.requireNonNullElse(properties.getAccessTokenMinutes(), 0L) <= 0) {
            throw new IllegalStateException("Configuration tradex.jwt.access-token-minutes must be greater than zero");
        }
        if (Objects.requireNonNullElse(properties.getRefreshTokenDays(), 0L) <= 0) {
            throw new IllegalStateException("Configuration tradex.jwt.refresh-token-days must be greater than zero");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
