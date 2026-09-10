package in.shubhamprakash681.api_gateway;

import in.shubhamprakash681.common_lib.security.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtGatewayFilterTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private GatewayFilterChain filterChain;

    private JwtGatewayFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtGatewayFilter(jwtTokenService);
    }

    @Test
    void allowsPublicStocksEndpointWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/stocks?page=0&size=8").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, filterChain).block();

        verify(filterChain).filter(exchange);
    }

    @Test
    void allowsPublicPricesLatestEndpointWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/prices/latest").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, filterChain).block();

        verify(filterChain).filter(exchange);
    }

    @Test
    void allowsWebSocketEndpointWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/ws").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, filterChain).block();

        verify(filterChain).filter(exchange);
    }

    @Test
    void rejectsStocksSubpathsWithoutToken() {
        MockServerHttpRequest searchRequest = MockServerHttpRequest.get("/api/stocks/search?q=tcs").build();
        MockServerWebExchange searchExchange = MockServerWebExchange.from(searchRequest);

        filter.filter(searchExchange, filterChain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, searchExchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(searchExchange);

        MockServerHttpRequest symbolRequest = MockServerHttpRequest.get("/api/stocks/RELIANCE").build();
        MockServerWebExchange symbolExchange = MockServerWebExchange.from(symbolRequest);

        filter.filter(symbolExchange, filterChain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, symbolExchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(symbolExchange);
    }

    @Test
    void rejectsMarketEndpointsWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/market/trending").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(exchange);
    }

    @Test
    void rejectsPricesSubpathsWithoutToken() {
        MockServerHttpRequest historyRequest = MockServerHttpRequest.get("/api/prices/history").build();
        MockServerWebExchange historyExchange = MockServerWebExchange.from(historyRequest);

        filter.filter(historyExchange, filterChain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, historyExchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(historyExchange);

        MockServerHttpRequest symbolRequest = MockServerHttpRequest.get("/api/prices/RELIANCE").build();
        MockServerWebExchange symbolExchange = MockServerWebExchange.from(symbolRequest);

        filter.filter(symbolExchange, filterChain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, symbolExchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(symbolExchange);
    }

    @Test
    void rejectsProtectedEndpointsWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/portfolio").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(exchange);
    }

    @Test
    void rejectsAdminMarketEndpointWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/admin/market/status").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(exchange);
    }
}
