package in.shubhamprakash681.portfolio_service.controllers;

import in.shubhamprakash681.common_lib.security.JwtPrincipal;
import in.shubhamprakash681.portfolio_service.dtos.PortfolioDtos;
import in.shubhamprakash681.portfolio_service.services.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
public class PortfolioController {
    private final PortfolioService portfolioService;

    @GetMapping
    PortfolioDtos.PortfolioResponse portfolio(@AuthenticationPrincipal JwtPrincipal principal,
                                              @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return portfolioService.portfolio(principal, authorization);
    }

    @GetMapping("/summary")
    PortfolioDtos.PortfolioSummaryResponse summary(@AuthenticationPrincipal JwtPrincipal principal,
                                                   @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return portfolioService.summary(principal, authorization);
    }

    @GetMapping("/holdings")
    List<PortfolioDtos.HoldingResponse> holdings(@AuthenticationPrincipal JwtPrincipal principal,
                                                 @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return portfolioService.holdings(principal, authorization);
    }
}
