package in.shubhamprakash681.portfolio_service.controllers;

import in.shubhamprakash681.common_lib.security.JwtPrincipal;
import in.shubhamprakash681.portfolio_service.dtos.TransactionResponse;
import in.shubhamprakash681.portfolio_service.services.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final PortfolioService portfolioService;

    @GetMapping
    Page<TransactionResponse> transactions(@AuthenticationPrincipal JwtPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return portfolioService.transactions(principal, pageable);
    }
}
