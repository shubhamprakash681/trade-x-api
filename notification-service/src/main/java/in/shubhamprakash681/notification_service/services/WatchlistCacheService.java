package in.shubhamprakash681.notification_service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.shubhamprakash681.notification_service.config.NotificationProperties;
import in.shubhamprakash681.notification_service.dtos.WatchlistDtos;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class WatchlistCacheService {
    private static final String KEY_PREFIX = "tradex:watchlist:";
    private static final TypeReference<List<WatchlistDtos.WatchlistResponse>> WATCHLIST_TYPE = new TypeReference<>() {
    };

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationProperties properties;

    public WatchlistCacheService(StringRedisTemplate redisTemplate,
                                 ObjectMapper objectMapper,
                                 NotificationProperties properties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    Optional<List<WatchlistDtos.WatchlistResponse>> get(Long userId) {
        try {
            String value = redisTemplate.opsForValue().get(key(userId));

            if (value != null) {
                return Optional.of(objectMapper.readValue(value, WATCHLIST_TYPE));
            }
        } catch (RuntimeException | JsonProcessingException ignored) {
            // Redis is a cache; falling back to the database keeps local development resilient.
        }

        return Optional.empty();
    }

    public void put(Long userId, List<WatchlistDtos.WatchlistResponse> watchlist) {
        try {
            redisTemplate.opsForValue().set(
                    key(userId),
                    objectMapper.writeValueAsString(watchlist),
                    Duration.ofMinutes(properties.getWatchlistCacheTtlMinutes())
            );
        } catch (RuntimeException | JsonProcessingException ignored) {
            // Cache write failures should not affect the user-facing watchlist operation.
        }
    }

    public void evict(Long userId) {
        try {
            redisTemplate.delete(key(userId));
        } catch (RuntimeException ignored) {
            // Cache eviction is best effort.
        }
    }

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }
}
