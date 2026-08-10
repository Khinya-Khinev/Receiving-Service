package com.waregang.receiving_service.common.idempotency;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final Duration KEY_EXPIRATION = Duration.ofMinutes(1);
    private static final String LOCKED_VALUE = "LOCKED";
    private final StringRedisTemplate redisTemplate;

    public boolean tryLock(String key) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, LOCKED_VALUE, KEY_EXPIRATION));
    }

    public void storeResponse(String key, String response) {
        redisTemplate.opsForValue().set(key, response, KEY_EXPIRATION);
    }

    public Optional<String> getResponse(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    public boolean isLocked(String value) {
        return LOCKED_VALUE.equals(value);
    }
}