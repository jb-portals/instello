package com.instello.backend.redis;

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
