package cn.loblok.upc.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁
 */
@Component
@Slf4j
public class RedisDistributedLock {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String LOCK_PREFIX = "lock:";
    private static final long DEFAULT_EXPIRE_TIME = 30; // 默认30秒

    public boolean tryLock(String key, long waitTime, TimeUnit unit) {
        String lockKey = LOCK_PREFIX + key;
        long startTime = System.currentTimeMillis();
        long timeout = unit.toMillis(waitTime);

        try {
            while (System.currentTimeMillis() - startTime < timeout) {
                Boolean success = redisTemplate.opsForValue()
                        .setIfAbsent(lockKey, "1", DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);

                if (Boolean.TRUE.equals(success)) {
                    return true;
                }

                Thread.sleep(100); // 短暂休眠后重试
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return false;
    }

    public void unlock(String key) {
        String lockKey = LOCK_PREFIX + key;
        redisTemplate.delete(lockKey);
    }
}