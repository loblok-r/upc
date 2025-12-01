package cn.loblok.upc.util;

import cn.loblok.upc.enums.UserLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CaculateUtils {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public  Integer getPoints(String scoreKey) {
        String pointsStr = redisTemplate.opsForValue().get(scoreKey);
        if (pointsStr == null) return 0;
        try {
            return Integer.parseInt(pointsStr);
        } catch (NumberFormatException e) {
            log.warn("积分Key格式异常: key={}, value={}", scoreKey, pointsStr);
            return 0;
        }
    }

    // 获取连续签到天数
    public  Integer getStreakDays(String streakKey) {
        Object streakObj = redisTemplate.opsForHash().get(streakKey, "current_streak");
        String streakStr = streakObj != null ? (String) streakObj : null;
        return streakStr != null ? Integer.parseInt(streakStr) : 0;
    }


    // 查询等级时（懒加载）
    public String getUserLevel(Long userId) {
        String cached = redisTemplate.opsForValue().get(RedisUtils.buildLevelKey(userId));
        if (cached != null) {
            return cached;
        }
        // 缓存 miss → 查积分 → 计算等级 → 写回缓存
        Integer points = getPoints(RedisUtils.buildScoreKey(userId));
        String level = calculateLevel(points);

        redisTemplate.opsForValue().set(RedisUtils.buildLevelKey(userId), level, 24L, TimeUnit.HOURS);
        return level;
    }

    /**
     * 计算用户等级
     * @param points
     * @return
     */
    public static String calculateLevel(int points){
        return UserLevel.getDesc(points);
    }
}