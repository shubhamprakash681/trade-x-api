package in.shubhamprakash681.price_stream_service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.shubhamprakash681.price_stream_service.dtos.PriceTick;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
// Service to cache PriceTick using Redis
public class PriceCacheService {
    private static final String LATEST_KEY_PREFIX = "tradex:prices:latest:";
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ConcurrentMap<String, PriceTick> localLatest = new ConcurrentHashMap<>();

    public PriceCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void put(PriceTick tick) {
        localLatest.put(tick.symbol(), tick);

        try {
            redisTemplate.opsForValue().set(LATEST_KEY_PREFIX + tick.symbol(), objectMapper.writeValueAsString(tick));
        } catch (RuntimeException | JsonProcessingException ignored) {
            // Redis is a cache; the in-memory copy keeps local development usable if Redis is down.
        }
    }

    public Optional<PriceTick> get(String symbol) {
        try {
            String value = redisTemplate.opsForValue().get(LATEST_KEY_PREFIX + symbol);

            if (value != null) {
                return Optional.of(objectMapper.readValue(value, PriceTick.class));
            }
        } catch (RuntimeException | JsonProcessingException ignored) {
            // Fall through to local cache.
        }

        return Optional.ofNullable(localLatest.get(symbol));
    }

    public List<PriceTick> all(Collection<String> symbols) {
        return symbols.stream()
                .map(this::get)
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(PriceTick::symbol))
                .toList();
    }
}
