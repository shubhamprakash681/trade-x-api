package in.shubhamprakash681.api_gateway;


import in.shubhamprakash681.common_lib.security.JwtTokenService;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {
    private static final List<String> CORS_RESPONSE_HEADERS = List.of(
            HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
            HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
            HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
            HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
            HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
            HttpHeaders.ACCESS_CONTROL_MAX_AGE);

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/signup",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/password-recovery/request",
            "/api/auth/password-recovery/reset",
            "/api/auth/logout",
            "/swagger-ui", // for swagger-ui page
            "/webjars/swagger-ui", // for swagger-ui resources
            "/v3/api-docs", // for swagger-config and grouped api-docs
            "/auth/v3/api-docs",
            "/market/v3/api-docs",
            "/portfolio/v3/api-docs",
            "/prices/v3/api-docs",
            "/notifications/v3/api-docs",
            "/ws");

    private final JwtTokenService jwtTokenService;

    public JwtGatewayFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    private boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] body = ("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}")
                .getBytes(StandardCharsets.UTF_8);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }

    @Override
    public Mono<Void> filter(@NotNull ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        // Both the gateway and an upstream Spring service may add CORS headers.
        // Normalize them just before the response is written; browsers reject
        // duplicate Access-Control-Allow-Origin values, even when identical.
        exchange.getResponse().beforeCommit(() -> {
            deduplicateCorsHeaders(exchange.getResponse().getHeaders());
            return Mono.empty();
        });

        String path = exchange.getRequest().getURI().getPath();
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS || isPublic(path)) return chain.filter(exchange);

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer "))
            return unauthorized(exchange, "Missing bearer token");

        String token = authorization.substring(7);
        try {
            var parsedToken = jwtTokenService.parse(token);
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-User-Id", String.valueOf(parsedToken.userId()))
                    .header("X-User-Email", parsedToken.email())
                    .header("X-User-Roles", String.join(",", parsedToken.roles()))
                    .build();

            return chain.filter(exchange.mutate().request(request).build());
        } catch (RuntimeException exception) {
            return unauthorized(exchange, "Invalid bearer token");
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private void deduplicateCorsHeaders(HttpHeaders headers) {
        for (String header : CORS_RESPONSE_HEADERS) {
            List<String> values = headers.get(header);
            if (values != null && values.size() > 1) {
                headers.set(header, values.getFirst());
            }
        }
    }
}
