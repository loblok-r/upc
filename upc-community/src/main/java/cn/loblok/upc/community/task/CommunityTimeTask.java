package cn.loblok.upc.community.task;

import cn.loblok.upc.common.utils.KeyUtils;
import cn.loblok.upc.community.entity.Posts;
import cn.loblok.upc.community.mapper.PostsMapper;
import cn.loblok.upc.community.service.PostsService;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@AllArgsConstructor
public class CommunityTimeTask {

    private static final String LEADERBOARD_KEY = KeyUtils.buildCommunityLeaderboardCreatorsKey();

    private final RedisTemplate<String, String> redisTemplate;

    private final PostsMapper postsMapper;

    /**
     * 每天凌晨清理负分用户
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanNegativeScoreUsersFromLeaderboard() {
        log.info("开始删除无效用户...");
        Long removed = redisTemplate.opsForZSet().removeRangeByScore(LEADERBOARD_KEY, Double.NEGATIVE_INFINITY, 0);
    }


    /**
     * 每10分钟执行一次
     * 定时任务：更新最新,推荐的各自1000条posts到redis缓存
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void syncPostsCaches() {
        String latestkey = KeyUtils.buildPostTabLatestPostKey();
        String recommendkey = KeyUtils.buildPostTabRecommendPostKey();
        String latestTempKey = latestkey + ":temp"; // 使用临时 Key 避免删除瞬间的击穿
        String recommendTempKey = recommendkey + ":temp";

        // 查出最近一段时间内最热的 2000 条数据
        List<Posts> hotPosts = postsMapper.getHotAndLatestPosts(2000);


        if (CollectionUtils.isEmpty(hotPosts)) {
            log.info("没有数据需要同步");
            return;
        }

        // 准备批量数据
        Set<ZSetOperations.TypedTuple<String>> latestTuples = new HashSet<>();
        Set<ZSetOperations.TypedTuple<String>> recommendTuples = new HashSet<>();
        ZoneId zoneId = ZoneId.systemDefault();

        for (Posts post : hotPosts) {
            String postId = post.getId().toString();
            long ts = post.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            latestTuples.add(new DefaultTypedTuple<>(postId, (double) ts));

            double recommendScore = post.getLikesCount().doubleValue();
            recommendTuples.add(new DefaultTypedTuple<>(postId, recommendScore));
        }

        // 写入临时 Key 并设置过期时间（防止任务失败导致死数据）
        redisTemplate.opsForZSet().add(latestTempKey, latestTuples);
        redisTemplate.expire(latestTempKey, 15, TimeUnit.MINUTES);

        redisTemplate.opsForZSet().add(recommendTempKey, recommendTuples);
        redisTemplate.expire(latestTempKey, 15, TimeUnit.MINUTES);

        // 原子性的覆盖旧 Key
        // rename 操作在 Redis 中是毫秒级的，能保证切换瞬间缓存始终有数据
        redisTemplate.rename(latestTempKey, latestkey);
        redisTemplate.rename(recommendTempKey, recommendkey);

        log.info("定时任务：同步最新{}条posts完成", hotPosts.size());
    }

}