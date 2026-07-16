package in.shubhamprakash681.portfolio_service.repositories;

import in.shubhamprakash681.portfolio_service.entity.PortfolioAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PortfolioAccountRepository extends JpaRepository<PortfolioAccount, Long> {
    Optional<PortfolioAccount> findByUserId(Long userId);
}
