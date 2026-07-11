package in.shubhamprakash681.auth_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import in.shubhamprakash681.auth_service.entity.RevokedToken;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {

}
