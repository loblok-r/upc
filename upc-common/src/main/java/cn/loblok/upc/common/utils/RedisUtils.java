package cn.loblok.upc.common.utils;


import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class RedisUtils {








    // 支持 StringRedisTemplate
    public static void setValue(StringRedisTemplate redisTemplate,
                                String key, boolean value, Duration expire) {
        redisTemplate.opsForValue().set(key, String.valueOf(value),
                expire.toMillis(), TimeUnit.MILLISECONDS);
    }

    public static void setValue(StringRedisTemplate redisTemplate,
                                String key, boolean value, long expireSeconds) {
        redisTemplate.opsForValue().set(key, String.valueOf(value), expireSeconds, TimeUnit.SECONDS);
    }

    // 或者更通用：支持任意 value（转成 String）
    public static void setValue(StringRedisTemplate redisTemplate,
                                String key, Object value, Duration expire) {
        redisTemplate.opsForValue().set(key, String.valueOf(value),
                expire.toMillis(), TimeUnit.MILLISECONDS);
    }


}