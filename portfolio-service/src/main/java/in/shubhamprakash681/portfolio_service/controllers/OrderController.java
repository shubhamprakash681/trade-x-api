package in.shubhamprakash681.portfolio_service.controllers;

import in.shubhamprakash681.common_lib.security.JwtPrincipal;
import in.shubhamprakash681.portfolio_service.dtos.OrderDtos;
import in.shubhamprakash681.portfolio_service.services.PortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final PortfolioService portfolioService;

    @PostMapping("/buy")
    OrderDtos.OrderResponse buy(@AuthenticationPrincipal JwtPrincipal principal,
                                @Valid @RequestBody OrderDtos.OrderRequest request,
                                @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return portfolioService.buy(principal, request, authorization);
    }

    @PostMapping("/sell")
    OrderDtos.OrderResponse sell(@AuthenticationPrincipal JwtPrincipal principal,
                                 @Valid @RequestBody OrderDtos.OrderRequest request,
                                 @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return portfolioService.sell(principal, request, authorization);
    }

    @GetMapping("/history")
    List<OrderDtos.OrderResponse> history(@AuthenticationPrincipal JwtPrincipal principal) {
        return portfolioService.orderHistory(principal);
    }
}
