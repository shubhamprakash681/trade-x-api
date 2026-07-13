package in.shubhamprakash681.portfolio_service.services;

import in.shubhamprakash681.common_lib.security.JwtPrincipal;
import in.shubhamprakash681.portfolio_service.dtos.OrderDtos;
import in.shubhamprakash681.portfolio_service.dtos.PortfolioDtos;
import in.shubhamprakash681.portfolio_service.dtos.TransactionResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortfolioService {
    public PortfolioDtos.PortfolioResponse portfolio(JwtPrincipal principal, String authorization) {
        return null;
    }

    public PortfolioDtos.PortfolioSummaryResponse summary(JwtPrincipal principal, String authorization) {
        return null;
    }

    public List<PortfolioDtos.HoldingResponse> holdings(JwtPrincipal principal, String authorization) {
        return null;
    }

    public OrderDtos.OrderResponse buy(JwtPrincipal principal, OrderDtos.OrderRequest request, String authorization) {
        return null;
    }

    public OrderDtos.OrderResponse sell(JwtPrincipal principal, OrderDtos.OrderRequest request, String authorization) {
        return null;
    }

    public List<OrderDtos.OrderResponse> orderHistory(JwtPrincipal principal) {
        return null;
    }

    public List<TransactionResponse> transactions(JwtPrincipal principal) {
        return null;
    }
}
