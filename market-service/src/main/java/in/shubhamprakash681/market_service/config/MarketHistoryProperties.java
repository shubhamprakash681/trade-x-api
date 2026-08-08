package in.shubhamprakash681.market_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@ConfigurationProperties(prefix = "tradex.market.history")
@Data
public class MarketHistoryProperties {
    private int years = 10;
    private Interval interval = Interval.DAILY;
    private LocalDate startDate;

    public LocalDate startDate(LocalDate today) {
        return startDate == null ? today.minusYears(years) : startDate;
    }

    public LocalDateTime startTime(LocalDate today) {
        return startDate(today).atStartOfDay();
    }

    public LocalDateTime endTime(LocalDateTime now) {
        return interval.normalizeEnd(now);
    }

    public long expectedCandleCount(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            return 0;
        }

        long count = 0;
        LocalDateTime candleTime = startTime;

        while (!candleTime.isAfter(endTime)) {
            count++;
            candleTime = interval.next(candleTime);
        }

        if (!interval.isAligned(startTime, endTime)) {
            count++;
        }

        return count;
    }

    public enum Interval {
        MINUTE {
            @Override
            public LocalDateTime next(LocalDateTime time) {
                return time.plusMinutes(1);
            }
        },
        HOURLY {
            @Override
            public LocalDateTime next(LocalDateTime time) {
                return time.plusHours(1);
            }
        },
        DAILY {
            @Override
            public LocalDateTime next(LocalDateTime time) {
                return time.plusDays(1);
            }
        },
        WEEKLY {
            @Override
            public LocalDateTime next(LocalDateTime time) {
                return time.plusWeeks(1);
            }
        },
        MONTHLY {
            @Override
            public LocalDateTime next(LocalDateTime time) {
                return time.plusMonths(1);
            }
        };

        public abstract LocalDateTime next(LocalDateTime time);

        LocalDateTime normalizeEnd(LocalDateTime time) {
            return switch (this) {
                case DAILY, WEEKLY, MONTHLY -> time.toLocalDate().atStartOfDay();
                case HOURLY -> time.truncatedTo(ChronoUnit.HOURS);
                case MINUTE -> time.truncatedTo(ChronoUnit.MINUTES);
            };
        }

        boolean isAligned(LocalDateTime startTime, LocalDateTime endTime) {
            LocalDateTime time = startTime;

            while (time.isBefore(endTime)) {
                time = next(time);
            }

            return time.equals(endTime);
        }
    }
}
