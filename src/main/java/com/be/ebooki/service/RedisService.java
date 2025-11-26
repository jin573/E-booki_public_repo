package com.be.ebooki.service;

import com.be.ebooki.domain.Team;
import com.be.ebooki.repository.TeamRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final TeamRepository teamRepository;

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
        final LocalDateTime setTTL = now.plusMinutes(1);
        return Duration.between(now, setTTL);
    }

    public boolean setIfAbsent(String key, String value, Duration duration) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(key, value, duration)
        );
    }

    //token으로 teamId = key 찾기
    public Integer findByTeamByToken(String token){
        ScanOptions scanOptions = ScanOptions.scanOptions().match("*").count(10).build();
        Cursor<byte[]> keys = redisTemplate.getConnectionFactory().getConnection().scan(scanOptions);

        while (keys.hasNext()){
            String key = new String(keys.next());
            String value = getValues(key);
            if(value != null || value.equals(token)){
                return Integer.valueOf(
                        key.replace("invite:team:", ""));
            }
        }
        return null;
    }

}