package in.shubhamprakash681.market_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.cloud.config.enabled=false",
		"spring.cloud.discovery.enabled=false",
		"eureka.client.enabled=false",
		"spring.datasource.url=jdbc:h2:mem:market_service_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"tradex.market.history.years=0",
		"tradex.jwt.issuer=tradex-test",
		"tradex.jwt.secret=tradex-test-secret-that-is-long-enough-for-hmac-signing",
		"tradex.jwt.access-token-minutes=30",
		"tradex.jwt.refresh-token-days=7"
})
class MarketServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
