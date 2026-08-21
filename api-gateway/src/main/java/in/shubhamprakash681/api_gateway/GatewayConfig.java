package in.shubhamprakash681.api_gateway;

import in.shubhamprakash681.common_lib.security.JwtProperties;
import in.shubhamprakash681.common_lib.security.JwtTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class GatewayConfig {
    @Bean
    JwtTokenService jwtTokenService(JwtProperties properties) {
        return new JwtTokenService(properties);
    }

    @Bean
    RouteLocator tradexRoutes(RouteLocatorBuilder builder,
                              @Value("${tradex.services.auth:lb://auth-service}") String authServiceUrl,
                              @Value("${tradex.services.market:lb://market-service}") String marketServiceUrl,
                              @Value("${tradex.services.portfolio:lb://portfolio-service}") String portfolioServiceUrl,
                              @Value("${tradex.services.price-stream:lb://price-stream-service}") String priceStreamServiceUrl,
                              @Value("${tradex.services.price-stream-ws:lb:ws://price-stream-service}") String priceStreamWebSocketUrl,
                              @Value("${tradex.services.notification:lb://notification-service}") String notificationServiceUrl) {
        return builder.routes()
                .route("auth-api", route -> route
                        .path("/api/auth/**", "/api/users/**")
                        .uri(authServiceUrl))
                .route("stock-api", route -> route
                        .path("/api/stocks/**")
                        .uri(marketServiceUrl))
                .route("market-api", route -> route
                        .path("/api/market/**")
                        .uri(marketServiceUrl))
                .route("market-admin-api", route -> route
                        .path("/api/admin/market/**")
                        .uri(marketServiceUrl))
                .route("portfolio-api", route -> route
                        .path("/api/portfolio/**", "/api/orders/**", "/api/transactions/**")
                        .uri(portfolioServiceUrl))
                .route("price-api", route -> route
                        .path("/api/prices/**")
                        .uri(priceStreamServiceUrl))
                .route("price-ws", route -> route
                        .path("/ws", "/ws/**")
                        .uri(priceStreamWebSocketUrl))
                .route("notification-api", route -> route
                        .path("/api/watchlist", "/api/watchlist/**", "/api/alerts", "/api/alerts/**",
                                "/api/notifications", "/api/notifications/**", "/api/dashboard", "/api/dashboard/**")
                        .uri(notificationServiceUrl))
                .route("auth-openapi", route -> route
                        .path("/auth/v3/api-docs")
                        .filters(filter -> filter.preserveHostHeader().rewritePath("/auth/v3/api-docs",
                                "/v3/api-docs"))
                        .uri(authServiceUrl))
                .route("market-openapi", route -> route
                        .path("/market/v3/api-docs")
                        .filters(filter -> filter.preserveHostHeader().rewritePath("/market/v3/api-docs",
                                "/v3/api-docs"))
                        .uri(marketServiceUrl))
                .route("portfolio-openapi", route -> route
                        .path("/portfolio/v3/api-docs")
                        .filters(filter -> filter.preserveHostHeader().rewritePath("/portfolio/v3/api-docs",
                                "/v3/api-docs"))
                        .uri(portfolioServiceUrl))
                .route("prices-openapi", route -> route
                        .path("/prices/v3/api-docs")
                        .filters(filter -> filter.preserveHostHeader().rewritePath("/prices/v3/api-docs",
                                "/v3/api-docs"))
                        .uri(priceStreamServiceUrl))
                .route("notifications-openapi", route -> route
                        .path("/notifications/v3/api-docs")
                        .filters(filter -> filter.preserveHostHeader().rewritePath("/notifications/v3/api-docs",
                                "/v3/api-docs"))
                        .uri(notificationServiceUrl))
                .build();
    }

    @Bean
    CorsWebFilter corsWebFilter() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type", "Location"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return new CorsWebFilter(source);
    }
}
