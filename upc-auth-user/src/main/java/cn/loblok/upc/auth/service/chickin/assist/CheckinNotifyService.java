package cn.loblok.upc.auth.service.chickin.assist;

import cn.loblok.upc.api.worker.dto.ExpTransactionDTO;
import cn.loblok.upc.api.worker.dto.PointTransactionDTO;
import cn.loblok.upc.auth.common.util.CacheUtils;
import cn.loblok.upc.auth.dto.chickin.RewardResult;
import cn.loblok.upc.auth.service.chickin.impl.LeaderboardServiceImpl;
import cn.loblok.upc.common.enums.BizType;
import cn.loblok.upc.common.utils.KeyUtils;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@Service
public class CheckinNotifyService {
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;
    private final LeaderboardServiceImpl leaderboardService;

    private static final int SCOREKEY_EXPIRE_DAYS = 30;
    private static final int EXPKEY_EXPIRE_DAYS = 30;

    private static final ZoneId BUSINESS_TIMEZONE = ZoneId.of("Asia/Shanghai");


    public void syncCacheAndNotify(String tenantId, Long userId, long recordId, RewardResult rewards) {
        // 同步 Redis 资产缓存 (使用 set 覆盖或 increment 增量)
        //直接覆盖，防止 DB 与 Redis 出现长期不一致
        redisTemplate.opsForValue().set(KeyUtils.buildPointsKey(userId), String.valueOf(rewards.getFinalTotalPoints()), SCOREKEY_EXPIRE_DAYS, TimeUnit.DAYS);
        redisTemplate.opsForValue().set(KeyUtils.buildExpKey(userId), String.valueOf(rewards.getNewTotalExp()), EXPKEY_EXPIRE_DAYS, TimeUnit.DAYS);

        // 标记今日已签到缓存
        String statusKey = KeyUtils.buildCheckinStatusKey(userId) + ":" + LocalDate.now(BUSINESS_TIMEZONE);
        redisTemplate.opsForValue().set(statusKey, "true", CacheUtils.getSecondsUntilEndOfDay(), TimeUnit.SECONDS);

        // 等级提升处理
        if (!rewards.getOldLevel().equals(rewards.getNewLevel())) {
            redisTemplate.delete(KeyUtils.buildLevelKey(userId)); // 删缓存触发懒加载
//            eventPublisher.publishEvent(new UserLevelUpgradedEvent(this, userId, oldLevel, newLevel));
        }

        // MQ 发送积分流水 (基础奖励)
        sendPointsLogMsg(tenantId, userId, recordId, rewards.getBasePoints(),rewards.getTotalPoints(), BizType.DAILY_SIGN.getDesc());

        //  MQ 发送积分流水 (额外奖励)
        if (rewards.isHasBonus()) {
            sendPointsLogMsg(tenantId, userId, recordId, rewards.getBonusPoints(),rewards.getFinalTotalPoints(), BizType.STREAK_SIGN.getDesc());
        }

        //  MQ 发送经验流水
        sendExpLogMsg(tenantId, userId, recordId, rewards.getAddedExp(),rewards.getNewTotalExp(), BizType.DAILY_SIGN.getDesc());

        //  更新排行榜
        leaderboardService.updateLeaderboardScore(tenantId, userId, rewards.getTotalPoints());
    }

    /**
     * 发送积分流水消息
     * @param tenantId
     * @param userId
     * @param recordId
     * @param basePoints
     */
    public void sendPointsLogMsg(String tenantId, Long userId, long recordId, int basePoints, int finalPoints,String biztype){
        PointTransactionDTO pointTransactionDTO = new PointTransactionDTO();
        pointTransactionDTO.builder()
                .tenantId(tenantId)
                .userId(userId)
                .bizType(biztype)
                .bizId(recordId)
                .deltaPoints(basePoints) //本次变动的积分值
                .totalPoints(finalPoints)
                .occurredAt(LocalDateTime.now()).build();

        rabbitTemplate.convertAndSend("upc.direct.exchange","mq.route.point_transaction", pointTransactionDTO);
    }


    /**
     * 发送经验流水消息
     * @param tenantId
     * @param userId
     * @param recordId
     * @param baseExp
     * @param biztype
     */
    public void sendExpLogMsg(String tenantId, Long userId, long recordId, int baseExp,int finalexps,String biztype){
        // 这里应该是ExpTransactionDTO，但根据上下文使用PointTransactionDTO
        ExpTransactionDTO expTransactionDTO = new ExpTransactionDTO();
        expTransactionDTO.builder()
                .tenantId(tenantId)
                .userId(userId)
                .bizType(biztype)
                .bizId(recordId)
                .deltaExps(baseExp) //本次变动的经验值
                .totalExps(finalexps)
                .occurredAt(LocalDateTime.now()).build();
        rabbitTemplate.convertAndSend("upc.direct.exchange","mq.route.exps_transaction", expTransactionDTO);
    }

}