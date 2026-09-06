package in.shubhamprakash681.portfolio_service.controllers;

import in.shubhamprakash681.common_lib.security.JwtPrincipal;
import in.shubhamprakash681.portfolio_service.dtos.OrderDtos;
import in.shubhamprakash681.portfolio_service.services.PortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final PortfolioService portfolioService;

    @PostMapping("/buy")
    OrderDtos.OrderResponse buy(@AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody OrderDtos.OrderRequest request) {
        return portfolioService.buy(principal, request);
    }

    @PostMapping("/sell")
    OrderDtos.OrderResponse sell(@AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody OrderDtos.OrderRequest request) {
        return portfolioService.sell(principal, request);
    }

    @GetMapping("/history")
    Page<OrderDtos.OrderResponse> history(@AuthenticationPrincipal JwtPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return portfolioService.orderHistory(principal, pageable);
    }
}
