package com.instello.backend.common.redis;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service 
public class RedisService {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisService(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set(String key, String value, Duration ttl) throws IllegalArgumentException{
        stringRedisTemplate.opsForValue().set(key, value, ttl);
    }

    public boolean setIfAbsent(String key, String value, Duration ttl){
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, value, ttl));
    }

    public long increment(String key, Duration ttlOnCreate){
        Long value = stringRedisTemplate.opsForValue().increment(key);
        long count = value != null ? value : 0;
        if (count == 1) {
            stringRedisTemplate.expire(key, ttlOnCreate);
        }
        return count;
    }

    public Optional<String> get(String key){
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(key));
    }

    public boolean delete(String key){
        return stringRedisTemplate.delete(key);
    }

    public Long getRemainingExpiration(String key){
        Long ttl = stringRedisTemplate.getExpire(key);
        return ttl !=null ? ttl : -2;
    }
}
