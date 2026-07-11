package in.shubhamprakash681.auth_service.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "revoked_tokens")
public class RevokedToken {
    @Id
    @Column(length = 128)
    private String tokenHash;

    @Column(nullable = false)
    private Instant revokedAt = Instant.now();

    public RevokedToken(String tokenHash) {
        this.tokenHash = tokenHash;
    }
}
