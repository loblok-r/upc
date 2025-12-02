package cn.loblok.upc.service.impl;

import cn.loblok.upc.service.DailyQuotaService;
import cn.loblok.upc.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 日常额度服务实现
 */
@Service
@Slf4j
class DailyQuotaServiceImpl implements DailyQuotaService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final long EXPIRE_HOURS = 30; // 保留到第二天中午，避免时区问题

    @Override
    public int getUsedToday(Long userId, String plan) {
        String key = RedisUtils.buildDailyQuotaKey(userId);
        String field = plan.toUpperCase();
        // ⚠️ opsForHash().get() 返回 Object，需转为 String
        Object valObj = redisTemplate.opsForHash().get(key, field);
        if (valObj == null) {
            return 0;
        }
        // 安全转换：确保是 String
        String valStr = valObj instanceof String ? (String) valObj : valObj.toString();
        try {
            return Integer.parseInt(valStr);
        } catch (NumberFormatException e) {
            log.warn("Redis quota value is not a number, key={}, field={}", key, field);
            return 0;
        }
    }


    public void incrementUsed(Long userId, String plan) {
        String key = RedisUtils.buildDailyQuotaKey(userId);
        String field = plan.toUpperCase();
        // 原子递增
        redisTemplate.opsForHash().increment(key, field, 1);
        // 首次设置过期时间（后续递增不会重置 TTL）
        redisTemplate.expire(key, EXPIRE_HOURS, TimeUnit.HOURS);
    }

}