package in.shubhamprakash681.auth_service.repositories;

import in.shubhamprakash681.auth_service.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findByEmail(String email);
    void deleteByEmail(String email);
}
