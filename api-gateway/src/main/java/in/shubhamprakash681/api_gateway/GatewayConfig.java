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
                              @Value("${tradex.services.portfolio:lb://portfolio-service}") String portfolioServiceUrl
    ) {
        return builder.routes()
                .route("auth-api", route -> route
                        .path("/api/auth/**", "/api/users/**")
                        .uri(authServiceUrl))
                .route("stock-api", route -> route
                        .path("/api/stocks/**")
                        .uri(marketServiceUrl))
                .route("portfolio-api", route -> route
                        .path("/portfolio/**", "/orders/**", "/transactions/**")
                        .uri(portfolioServiceUrl))
                .route("auth-openapi", route -> route
                        .path("/auth/v3/api-docs")
                        .filters(filter -> filter.rewritePath("/auth/v3/api-docs", "/v3/api-docs"))
                        .uri(authServiceUrl))
                .route("market-openapi", route -> route
                        .path("/market/v3/api-docs")
                        .filters(filter -> filter.rewritePath("/market/v3/api-docs", "/v3/api-docs"))
                        .uri(marketServiceUrl))
                .route("portfolio-openapi", route -> route
                        .path("/portfolio/v3/api-docs")
                        .filters(filter -> filter.rewritePath("/portfolio/v3/api-docs", "/v3/api-docs"))
                        .uri(portfolioServiceUrl))
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
