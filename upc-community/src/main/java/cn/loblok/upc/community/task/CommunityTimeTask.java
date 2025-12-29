package cn.loblok.upc.community.task;

import cn.loblok.upc.common.utils.KeyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CommunityTimeTask {

    private static final String LEADERBOARD_KEY = KeyUtils.buildCommunityLeaderboardCreatorsKey();
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 每天凌晨清理负分用户
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanNegativeScoreUsersFromLeaderboard() {
        log.info("开始删除无效用户...");
        Long removed = redisTemplate.opsForZSet().removeRangeByScore(LEADERBOARD_KEY, Double.NEGATIVE_INFINITY, 0);
    }
}