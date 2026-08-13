package in.shubhamprakash681.notification_service.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class WatchlistDtos {
    public record AddWatchlistRequest(
            @NotBlank @Size(max = 32) String symbol
    ) {
    }

    public record WatchlistResponse(
            Long id,
            String symbol,
            String stockName,
            String exchange,
            LocalDateTime createdAt
    ) {
    }
}
