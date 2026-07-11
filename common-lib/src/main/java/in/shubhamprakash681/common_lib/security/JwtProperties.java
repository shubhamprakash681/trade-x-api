package in.shubhamprakash681.common_lib.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tradex.jwt")
public class JwtProperties {
    private String issuer;
    private String secret;
    private Long accessTokenMinutes;
    private Long refreshTokenDays;

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getAccessTokenMinutes() {
        return accessTokenMinutes;
    }

    public void setAccessTokenMinutes(Long accessTokenMinutes) {
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public Long getRefreshTokenDays() {
        return refreshTokenDays;
    }

    public void setRefreshTokenDays(Long refreshTokenDays) {
        this.refreshTokenDays = refreshTokenDays;
    }
}
