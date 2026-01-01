package cn.loblok.upc.common.utils;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnClass(RedisTemplate.class)
public class RedisUtils {
    private static RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisTemplate<String, Object> instanceRedisTemplate;

    @PostConstruct
    public void init() {
        this.redisTemplate = instanceRedisTemplate;
    }
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

    public static List<Long> getIdsFromRedis(String key, int start, int end) {
        Set<Object> range = redisTemplate.opsForZSet().reverseRange(key, start, end);
        if (CollectionUtils.isEmpty(range)) {
            return Collections.emptyList();
        }
        return range.stream()
                .map(obj -> {
                    if (obj instanceof String) {
                        return Long.valueOf((String) obj);
                    } else if (obj instanceof Number) {
                        return ((Number) obj).longValue();
                    } else {
                        return Long.valueOf(obj.toString());
                    }
                })
                .toList();
    }


}