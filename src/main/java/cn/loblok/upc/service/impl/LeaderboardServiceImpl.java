package cn.loblok.upc.service.impl;

import cn.loblok.upc.dto.LeaderboardResponseDTO;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.service.LeaderboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class LeaderboardServiceImpl implements LeaderboardService {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    // 排行榜Key前缀 - 按月分榜
    private static final String LEADERBOARD_KEY_PREFIX = "leaderboard:point:";
    private static final String USER_PREFIX = "user:";

    private static final ZoneId BUSINESS_TIMEZONE = ZoneId.of("Asia/Shanghai");
    
    @Override
public Result<LeaderboardResponseDTO> getLeaderboard(String tenantId, Long userId) {
        log.info("开始获取排行榜数据: tenantId={}, userId={}", tenantId, userId);

        // 获取业务日期（按东八区）所有与“日期”相关的逻辑（签到、排行榜）都应使用同一时区！
        LocalDate businessDate = LocalDate.now(BUSINESS_TIMEZONE);
        // 获取当前月份作为分榜标识
        String currentMonth = businessDate.format(DateTimeFormatter.ofPattern("yyyyMM"));

        String leaderboardKey = LEADERBOARD_KEY_PREFIX + tenantId + ":" + currentMonth;
        
        // 获取Top 10用户
        Set<ZSetOperations.TypedTuple<String>> topUsers = redisTemplate.opsForZSet().reverseRangeWithScores(leaderboardKey, 0, 9);
        List<LeaderboardResponseDTO.LeaderboardItem> topList = new ArrayList<>();
        
        if (topUsers != null) {
            long rank = 1;
            for (ZSetOperations.TypedTuple<String> tuple : topUsers) {
                LeaderboardResponseDTO.LeaderboardItem item = new LeaderboardResponseDTO.LeaderboardItem();
                item.setUserId(tuple.getValue());
                item.setScore(tuple.getScore());
                item.setRank(rank++);
                topList.add(item);
            }
        }
        
        // 获取当前用户的排名和分数
        String currentUserKey = USER_PREFIX + userId;
        Long myRank = redisTemplate.opsForZSet().reverseRank(leaderboardKey, currentUserKey);
        Double myScore = redisTemplate.opsForZSet().score(leaderboardKey, currentUserKey);
        
        // 处理排名（Redis返回的排名从0开始，我们需要从1开始）
        // 如果用户没上榜，rank 和 score 都是 null
        if (myRank == null) {
            myRank = -1L; // 表示“未上榜”
            myScore = 0.0;
        } else {
            myRank++; // 转为 1-based
        }
        
        // 如果用户没有分数，设置默认值
        if (myScore == null) {
            myScore = 0.0;
        }
        LeaderboardResponseDTO leaderboardResponseDTO = new LeaderboardResponseDTO(topList, myRank, myScore);
        return Result.success(leaderboardResponseDTO);
    }
    
    /**
     * 更新用户积分到排行榜
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @param score 增加的分数
     */
    public void updateLeaderboardScore(String tenantId, Long userId, double score) {
        // 获取当前月份作为分榜标识
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String leaderboardKey = LEADERBOARD_KEY_PREFIX + tenantId + ":" + currentMonth;
        
        String userKey = USER_PREFIX + userId;
        redisTemplate.opsForZSet().incrementScore(leaderboardKey, userKey, score);
        
        log.info("更新用户积分到排行榜: tenantId={}, userId={}, score={}", tenantId, userId, score);
    }
}