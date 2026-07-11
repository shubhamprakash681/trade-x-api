package in.shubhamprakash681.api_gateway;

import in.shubhamprakash681.common_lib.security.JwtProperties;
import in.shubhamprakash681.common_lib.security.JwtTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    @Bean
    JwtTokenService jwtTokenService(JwtProperties properties) {
        return new JwtTokenService(properties);
    }

    @Bean
    RouteLocator tradexRoutes(RouteLocatorBuilder builder,
                              @Value("${tradex.services.auth:lb://auth-service}") String authServiceUrl,
                              @Value("${tradex.services.market:lb://market-service}") String marketServiceUrl
    ) {
        return builder.routes()
                .route("auth-api", route -> route
                        .path("/api/auth/**", "/api/users/**")
                        .uri(authServiceUrl))
                .route("stock-api", route -> route
                        .path("/api/stocks/**")
                        .uri(marketServiceUrl))
                .route("auth-openapi", route -> route
                        .path("/auth/v3/api-docs")
                        .filters(filter -> filter.rewritePath("/auth/v3/api-docs", "/v3/api-docs"))
                        .uri(authServiceUrl))
                .route("market-openapi", route -> route
                        .path("/market/v3/api-docs")
                        .filters(filter -> filter.rewritePath("/market/v3/api-docs", "/v3/api-docs"))
                        .uri(marketServiceUrl))
                .build();
    }
}
