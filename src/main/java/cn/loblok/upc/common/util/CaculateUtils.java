package cn.loblok.upc.common.util;

import cn.loblok.upc.common.enums.UserLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 计算工具类
 */
@Service
@Slf4j
public class CaculateUtils {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 获取积分 todo 可能要补充
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

    // 获取经验值 todo 可能要补充
    public  Integer getExps(String expsKey) {
        String expsStr = redisTemplate.opsForValue().get(expsKey);
        if (expsStr == null) return 0;
        try {
            return Integer.parseInt(expsStr);
        } catch (NumberFormatException e) {
            log.warn("经验Key格式异常: key={}, value={}", expsKey, expsStr);
            return 0;
        }
    }

    // 获取连续签到天数 todo 可能要补充
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
        Integer exps = getExps(RedisUtils.buildExpKey(userId));
        String level = calculateLevel(exps);

        redisTemplate.opsForValue().set(RedisUtils.buildLevelKey(userId), level, 24L, TimeUnit.HOURS);
        return level;
    }

    /**
     * 计算用户等级
     * @param exps
     * @return
     */
    public static String calculateLevel(int exps){
        return UserLevel.getDesc(exps);
    }
    
    /**
     * 将等级名称映射为数字
     * @param levelName 等级名称，如"LEVEL1"
     * @return 等级数字，如1；如果无法解析则返回0
     */
    public static int getLevelNumber(String levelName) {
        return UserLevel.getLevelNumber(levelName);
    }
}