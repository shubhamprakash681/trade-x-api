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
    private Integer days;
    private Integer hours;
    private Integer minutes;
    private Integer seconds;
    private Interval interval = Interval.DAILY;
    private LocalDate startDate;
    private LocalDateTime startDateTime;

    public LocalDate startDate(LocalDate today) {
        if (startDate != null) {
            return startDate;
        }
        if (days != null) {
            return today.minusDays(days);
        }
        return switch (interval) {
            case SECONDS, MINUTE -> today;
            case HOURLY -> today.minusDays(30);
            case DAILY, WEEKLY, MONTHLY -> today.minusYears(years);
        };
    }

    public LocalDateTime startTime(LocalDate today) {
        return startTime(today.atStartOfDay());
    }

    public LocalDateTime startTime(LocalDateTime now) {
        if (startDateTime != null) {
            return interval.normalizeStart(startDateTime);
        }
        if (startDate != null) {
            return startDate.atStartOfDay();
        }
        if (seconds != null) {
            return interval.normalizeStart(now.minusSeconds(seconds));
        }
        if (minutes != null) {
            return interval.normalizeStart(now.minusMinutes(minutes));
        }
        if (hours != null) {
            return interval.normalizeStart(now.minusHours(hours));
        }
        if (days != null) {
            return interval.normalizeStart(now.minusDays(days));
        }
        return switch (interval) {
            case SECONDS -> interval.normalizeStart(now.minusHours(1));
            case MINUTE -> interval.normalizeStart(now.minusDays(1));
            case HOURLY -> interval.normalizeStart(now.minusDays(30));
            case DAILY, WEEKLY, MONTHLY -> now.toLocalDate().minusYears(years).atStartOfDay();
        };
    }

    public LocalDateTime endTime(LocalDateTime now) {
        return interval.normalizeEnd(now);
    }

    public long expectedCandleCount(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            return 0;
        }

        long count = switch (interval) {
            case SECONDS -> ChronoUnit.SECONDS.between(startTime, endTime);
            case MINUTE -> ChronoUnit.MINUTES.between(startTime, endTime);
            case HOURLY -> ChronoUnit.HOURS.between(startTime, endTime);
            case DAILY -> ChronoUnit.DAYS.between(startTime, endTime);
            case WEEKLY -> ChronoUnit.WEEKS.between(startTime, endTime);
            case MONTHLY -> ChronoUnit.MONTHS.between(startTime, endTime);
        } + 1;

        if (!interval.isAligned(startTime, endTime)) {
            count++;
        }

        return count;
    }

    public enum Interval {
        SECONDS {
            @Override
            public LocalDateTime next(LocalDateTime time) {
                return time.plusSeconds(1);
            }

            @Override
            public double timeStepInDays() {
                return 1.0 / 86400.0;
            }
        },
        MINUTE {
            @Override
            public LocalDateTime next(LocalDateTime time) {
                return time.plusMinutes(1);
            }

            @Override
            public double timeStepInDays() {
                return 1.0 / 1440.0;
            }
        },
        HOURLY {
            @Override
            public LocalDateTime next(LocalDateTime time) {
                return time.plusHours(1);
            }

            @Override
            public double timeStepInDays() {
                return 1.0 / 24.0;
            }
        },
        DAILY {
            @Override
            public LocalDateTime next(LocalDateTime time) {
                return time.plusDays(1);
            }

            @Override
            public double timeStepInDays() {
                return 1.0;
            }
        },
        WEEKLY {
            @Override
            public LocalDateTime next(LocalDateTime time) {
                return time.plusWeeks(1);
            }

            @Override
            public double timeStepInDays() {
                return 7.0;
            }
        },
        MONTHLY {
            @Override
            public LocalDateTime next(LocalDateTime time) {
                return time.plusMonths(1);
            }

            @Override
            public double timeStepInDays() {
                return 30.0;
            }
        };

        public abstract LocalDateTime next(LocalDateTime time);

        public abstract double timeStepInDays();

        public LocalDateTime normalizeEnd(LocalDateTime time) {
            return switch (this) {
                case DAILY, WEEKLY, MONTHLY -> time.toLocalDate().atStartOfDay();
                case HOURLY -> time.truncatedTo(ChronoUnit.HOURS);
                case MINUTE -> time.truncatedTo(ChronoUnit.MINUTES);
                case SECONDS -> time.truncatedTo(ChronoUnit.SECONDS);
            };
        }

        public LocalDateTime normalizeStart(LocalDateTime time) {
            return switch (this) {
                case DAILY, WEEKLY, MONTHLY -> time.toLocalDate().atStartOfDay();
                case HOURLY -> time.truncatedTo(ChronoUnit.HOURS);
                case MINUTE -> time.truncatedTo(ChronoUnit.MINUTES);
                case SECONDS -> time.truncatedTo(ChronoUnit.SECONDS);
            };
        }

        public boolean isAligned(LocalDateTime startTime, LocalDateTime endTime) {
            if (startTime.isAfter(endTime)) {
                return false;
            }
            return switch (this) {
                case SECONDS -> ChronoUnit.NANOS.between(startTime, endTime) % 1_000_000_000L == 0;
                case MINUTE -> ChronoUnit.SECONDS.between(startTime, endTime) % 60 == 0;
                case HOURLY -> ChronoUnit.MINUTES.between(startTime, endTime) % 60 == 0;
                case DAILY -> ChronoUnit.HOURS.between(startTime, endTime) % 24 == 0;
                case WEEKLY -> ChronoUnit.DAYS.between(startTime, endTime) % 7 == 0;
                case MONTHLY -> startTime.plusMonths(ChronoUnit.MONTHS.between(startTime, endTime)).equals(endTime);
            };
        }
    }
}
