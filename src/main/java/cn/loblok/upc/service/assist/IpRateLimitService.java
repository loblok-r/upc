package cn.loblok.upc.service.assist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
/**
 * IP 访问限制服务
 */
@Service
public class IpRateLimitService {

    private static final String KEY_PREFIX = "rate_limit:ip:";
    private static final int MAX_REQUESTS_PER_DAY = 30;
    private static final long EXPIRE_DAYS = 1; // 自动过期，防止堆积

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 尝试为指定 IP 增加一次调用计数
     * @param ip 客户端 IP
     * @return true 表示允许调用；false 表示已超限
     */
    public boolean tryAcquire(String ip) {
        if (ip == null || ip.isBlank()) {
            return true; // 无法识别 IP 时放行（或根据策略拒绝）
        }

        String key = KEY_PREFIX + ip + ":" + LocalDate.now();
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            // 第一次访问，设置 24 小时过期（实际可设为到明天 0 点的秒数）
            redisTemplate.expire(key, EXPIRE_DAYS, TimeUnit.DAYS);
        }

        return count != null && count <= MAX_REQUESTS_PER_DAY;
    }

    /**
     * （可选）获取当前剩余次数
     */
    public long getRemaining(String ip) {
        String key = KEY_PREFIX + ip + ":" + LocalDate.now();
        String value = redisTemplate.opsForValue().get(key);
        long used = value == null ? 0 : Long.parseLong(value);
        return Math.max(0, MAX_REQUESTS_PER_DAY - used);
    }
}