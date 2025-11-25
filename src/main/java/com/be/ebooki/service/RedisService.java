package com.be.ebooki.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public void setValues(String key, String value, Duration duration){
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(key, value, duration);
    }

    public String getValues(String key) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        return (values.get(key) == null) ? "false" : values.get(key);
    }

    public static Duration expireTime() {
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        final LocalDateTime setTTL = now.plusMinutes(15);
        return Duration.between(now, setTTL);
    }

    public boolean setIfAbsent(String key, String value, Duration duration) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(key, value, duration)
        );
    }
}
