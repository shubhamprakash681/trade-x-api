package in.shubhamprakash681.api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "tradex.jwt.issuer=tradex-test",
        "tradex.jwt.secret=tradex-test-secret-that-is-long-enough-for-hmac-signing",
        "tradex.jwt.access-token-minutes=30",
        "tradex.jwt.refresh-token-days=7"
})
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
    }

}
