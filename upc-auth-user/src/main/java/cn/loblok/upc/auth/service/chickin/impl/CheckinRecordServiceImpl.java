package cn.loblok.upc.auth.service.chickin.impl;

import cn.loblok.upc.auth.entity.UserItems;
import cn.loblok.upc.auth.entity.UserPoints;
import cn.loblok.upc.auth.service.chickin.CheckinRecordService;
import cn.loblok.upc.auth.service.chickin.ExpTransactionService;
import cn.loblok.upc.auth.service.chickin.PointTransactionService;
import cn.loblok.upc.auth.service.chickin.assist.CheckinRewardRuleService;
import cn.loblok.upc.common.enums.UserItemType;
import cn.loblok.upc.auth.service.UserItemsService;
import cn.loblok.upc.auth.service.UserPointsService;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.auth.entity.chickin.CheckinRecord;
import cn.loblok.upc.common.enums.CommonStatusEnum;
import cn.loblok.upc.auth.mapper.CheckinRecordMapper;
import cn.loblok.upc.auth.dto.ValidationResult;
import cn.loblok.upc.auth.service.UserService;
import cn.loblok.upc.auth.dto.chickin.CheckinHistoryResponse;
import cn.loblok.upc.auth.dto.chickin.CheckinResponseDTO;
import cn.loblok.upc.auth.entity.User;
import cn.loblok.upc.common.enums.BizType;
import cn.loblok.upc.auth.common.util.CacheUtils;
import cn.loblok.upc.auth.common.util.CaculateUtils;
import cn.loblok.upc.common.utils.RedisUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * <p>
 * 签到记录服务实现
 * </p>
 *
 * @author loblok
 * @since 2025-11-30
 */
@Service
@Primary
@Slf4j
@AllArgsConstructor
public class CheckinRecordServiceImpl extends ServiceImpl<CheckinRecordMapper, CheckinRecord> implements CheckinRecordService {

    public static AtomicInteger dbQueryCount = new AtomicInteger(0);


    private final UserService userService;


    private final UserPointsService userPointsService;


    private final StringRedisTemplate redisTemplate;


    private final CheckinRecordMapper baseMapper;


    private final LeaderboardServiceImpl leaderboardService;


    private final UserItemsService userItemsService;


    private final PointTransactionService pointTransactionService;


    private final CheckinRewardRuleService rewardRuleService;


    private final ExpTransactionService expTransactionService;


    private final CaculateUtils caculateUtils;


    private ApplicationEventPublisher eventPublisher;

    private static final ZoneId BUSINESS_TIMEZONE = ZoneId.of("Asia/Shanghai");


    private static final int CHECKIN_STREAK_DAYS = 7;

    private static final int CHECKIN_STREAK_POINTS = 30;

    private static final int SCOREKEY_EXPIRE_DAYS = 30;
    private static final int EXPKEY_EXPIRE_DAYS = 30;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<CheckinResponseDTO> checkin(String tenantId, Long userId) {

        return executeCheckin(tenantId, userId, LocalDate.now());
    }

    @Override
    public Result<CheckinResponseDTO> executeCheckin(String tenantId, Long userId, LocalDate checkinDate) {
        log.info("开始处理签到请求: tenantId={}, userId={},chickinDate = {}", tenantId, userId, checkinDate);

        // 获取用户信息
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 获取当前日期
        String dateStr = checkinDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 构造 biz_key
        String bizKey = "checkin_" + tenantId + "_" + userId + "_" + dateStr;
        // 用户积分Key（使用Redis）
        String scoreKey = RedisUtils.buildPointsKey(user.getId());

        // 用户经验值Key
        String expKey = RedisUtils.buildExpKey(user.getId());
        // 用户连续签到信息Key
        String streakKey = RedisUtils.buildStreakKey(user.getId());

        String levelKey = RedisUtils.buildLevelKey(user.getId());

        // 检查是否已经签到
        QueryWrapper<CheckinRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("biz_key", bizKey);
        if (this.count(queryWrapper) > 0) {
            return Result.error(CommonStatusEnum.HasCheckedIn.getCode(), CommonStatusEnum.HasCheckedIn.getMessage());
        }

        // 创建签到记录
        CheckinRecord checkinRecord = new CheckinRecord();
        checkinRecord.setUserId(userId);
        checkinRecord.setTenantId(tenantId);
        checkinRecord.setCheckinDate(checkinDate);
        checkinRecord.setBizKey(bizKey);
        // 这里可以添加IP地址和User-Agent，但在实际应用中应该从请求中获取
        checkinRecord.setIpAddress("192.168.1.100"); // 示例IP地址
        checkinRecord.setUserAgent("Mozilla/5.0 ..."); // 示例User-Agent

        // 插入签到记录
        boolean saveResult = this.save(checkinRecord);
        if (!saveResult) {
            Integer points = caculateUtils.getPoints(scoreKey);
            Integer streakDays = caculateUtils.getStreakDays(streakKey);
            CheckinResponseDTO checkinResponseDTO = new CheckinResponseDTO(points, streakDays);
            return Result.error("签到失败");
        }


        CheckinRewardRuleService.RewardConfig dailyReward = rewardRuleService.getRewardByDate(checkinDate);
        int basePoints = dailyReward.getPoints();
        int baseExp = dailyReward.getExp();

        log.info("签到成功，获取积分：{}", basePoints);
        log.info("签到成功，获取经验值：{}", baseExp);

        // 缓存签到状态
        String cacheKey = RedisUtils.buildCheckinStatusKey(userId) + ":" + LocalDate.now(BUSINESS_TIMEZONE);
        long expireSecs = CacheUtils.getSecondsUntilEndOfDay();
        redisTemplate.opsForValue().set(cacheKey, "true", expireSecs, TimeUnit.SECONDS);

        // 增加用户积分后的积分
        Long pointsAfterBase = redisTemplate.opsForValue().increment(scoreKey, basePoints);


        // 获取升级前的真实经验值（关键！）
        String expStr = redisTemplate.opsForValue().get(expKey);
        int currentExpInRedis;
        if (expStr != null) {
            currentExpInRedis = Integer.parseInt(expStr);
        } else {
            // Redis 无缓存，从 DB 加载
            User userFromDb = userService.getById(userId);
            currentExpInRedis = (userFromDb != null) ? userFromDb.getExp() : 0;
        }

        // 增加用户经验值（基础5点）
        Long expsAfterBase = redisTemplate.opsForValue().increment(expKey, baseExp);

        // 删除用户等级缓存
        // 加完经验后，判断是否可能跨越等级阈值
        // 计算新旧等级（基于真实值）
        String oldLevel = caculateUtils.calculateLevel(currentExpInRedis);
        String newLevel = caculateUtils.calculateLevel(expsAfterBase.intValue());

        int oldLevelNum = parseLevelNumber(oldLevel);
        int newLevelNum = parseLevelNumber(newLevel);

        boolean levelUpgraded = newLevelNum > oldLevelNum;

        // 如果升级，删除缓存（支持懒加载）
        if (levelUpgraded) {
            redisTemplate.delete(levelKey);
        }


//         异步记录经验流水
        expTransactionService.asyncLog(
                tenantId, userId, BizType.DAILY_SIGN, checkinRecord.getId(), baseExp, expsAfterBase
        );

//         如果等级提升了，发布等级升级事件
//        if (levelUpgraded) {
//            UserLevelUpgradedEvent event = new UserLevelUpgradedEvent(
//                    this,
//                    userId,
//                    oldLevelNum,
//                    newLevelNum,
//                    oldLevel,
//                    newLevel
//            );
//            eventPublisher.publishEvent(event);
//        }

//         更新排行榜（基础10分）
//        leaderboardService.updateLeaderboardScore(tenantId, userId, CHECKIN_BASE_POINTS);

//         异步记录积分流水
        pointTransactionService.asyncLog(
                tenantId, userId, BizType.DAILY_SIGN, checkinRecord.getId(), basePoints, pointsAfterBase
        );

        // 计算连续签到天数
        Integer streakDays = calculateStreakDays(userId, checkinDate, streakKey);

        // 额外奖励：如果连续签到7天，额外增加50积分
        int bonusPoints = basePoints;
        Long pointsAfterBonus = pointsAfterBase;
        if (streakDays >= CHECKIN_STREAK_DAYS && streakDays % CHECKIN_STREAK_DAYS == 0) {
            pointsAfterBonus = redisTemplate.opsForValue().increment(scoreKey, CHECKIN_STREAK_POINTS);

            // 更新排行榜（额外50分）
            leaderboardService.updateLeaderboardScore(tenantId, userId, CHECKIN_STREAK_POINTS);
            // 异步记录积分流水
            pointTransactionService.asyncLog(
                    tenantId, userId, BizType.STREAK_SIGN, checkinRecord.getId(), CHECKIN_STREAK_POINTS, pointsAfterBonus
            );
            bonusPoints = bonusPoints + 50;
        }

        // 更新用户积分值
        Long finalPoints = pointsAfterBase;
        if (bonusPoints > 0) {
            finalPoints = pointsAfterBonus;
        }
//        log.info("更新用户积分值：{}", finalPoints);
        log.info("用户获得积分值：{}", bonusPoints);


        //更新用户积分表
        userPointsService.update(
                null,
                new UpdateWrapper<UserPoints>()
                        .eq("user_id", userId)
                        .setSql("balance = balance + " + bonusPoints +
                                ", total_earned = total_earned + " + bonusPoints +
                                ", total_spent = 0" +
                                ", updated_at = NOW()")
        );


        //更新用户表
        userService.update(
                null,
                new UpdateWrapper<User>()
                        .eq("id", userId)
                        .setSql("ischickined = 1, " +
                                "points = points + " + bonusPoints + ", " +
                                "exp = " + expsAfterBase.intValue() + ", " +
                                "user_level = '" + newLevel + "', " +
                                "streakdays = " + streakDays)
        );

        // 设置Redis键的过期时间（例如：30天）
        redisTemplate.expire(scoreKey, SCOREKEY_EXPIRE_DAYS, TimeUnit.DAYS);
        redisTemplate.expire(streakKey, EXPKEY_EXPIRE_DAYS, TimeUnit.DAYS);

        // 返回成功响应，包含最新的积分值和连续签到天数
        Integer points = caculateUtils.getPoints(scoreKey);
        CheckinResponseDTO checkinResponseDTO = new CheckinResponseDTO(points, streakDays);
        return Result.success(checkinResponseDTO);
    }

    /**
     * 签到（复签）
     *
     * @param userId    用户ID
     * @param retroDate 复签日期
     * @return 签到响应
     */


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<CheckinResponseDTO> reTroChickIn(String tenantId,Long userId, LocalDate retroDate) {


        try {

            // 验证和准备（无事务）
            ValidationResult validation = validateRetroCheckin(tenantId, userId, retroDate);
            if (!validation.isValid()) {
                return Result.error(validation.getMessage());
            }

            // 扣除补签卡（单独事务）
            boolean deductSuccess = deductRetroCard(userId);
            if (!deductSuccess) {
                return Result.error("补签卡不足");
            }

            log.info("执行复签");
            return executeCheckin(tenantId, userId, retroDate);


        } catch (Exception e) {
            log.error("复签失败", e);
            return Result.error("复签失败");
        }
    }

    // 验证补签
    private ValidationResult validateRetroCheckin(String tenantId, Long userId, LocalDate retroDate) {
        // 不能补签未来日期
        if (retroDate.isAfter(LocalDate.now())) {
            return new ValidationResult(false, "不能补签未来日期");
        }

        // 只能补签最近30天
        if (ChronoUnit.DAYS.between(retroDate, LocalDate.now()) > 30) {
            return new ValidationResult(false, "只能补签最近30天的日期");
        }

        // 检查是否已签到
        if (hasCheckedInToday(tenantId, userId, retroDate)) {
            return ValidationResult.error("该日期已签到");
        }

        return ValidationResult.success();
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public boolean deductRetroCard(Long userId) {


        log.info("开始扣减补签卡");
        List<UserItems> chances =  userItemsService.getByUserId(userId, UserItemType.RESIGN_CARD);

        // 检查补签
        if (chances.isEmpty()) {
            return false;
        }

        // todo 扣减次数，原子操作，生产环境 需要换数据库乐观锁或 Redis 分布式锁
        for (UserItems chance : chances) {
            // 尝试乐观锁扣减（推荐）
            int updated = userItemsService.consumeOneChanceWithOptimisticLock(chance.getId());
            if (updated > 0) {
                // 扣减成功，退出
               return true;
            }
            // 如果失败（quantity 被别人改了），继续下一条
        }
        return false;
    }


    @Override
    public boolean hasCheckedInToday(String tenantId, Long userId) {
        return hasCheckedInToday(tenantId, userId, LocalDate.now());
    }

    public boolean hasCheckedInToday(String tenantId, Long userId, LocalDate checkinDate) {
        log.info("开始处理查询今天是否已签到请求: tenantId={}, userId={}", tenantId, userId);
        String key = RedisUtils.buildCheckinStatusKey(userId) + LocalDate.now(BUSINESS_TIMEZONE);

        // 1. 查缓存
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return Boolean.parseBoolean(cached);
        }

        // 2. 缓存未命中 → 查 DB（击穿点！）
        QueryWrapper<CheckinRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tenant_id", tenantId)
                .eq("user_id", userId)
                .eq("checkin_date", checkinDate);

        // 记录一次 DB 查询
        dbQueryCount.incrementAndGet();
        boolean exists = this.exists(queryWrapper);

        // 计算今天还剩多少秒
        long expireSecs = CacheUtils.getSecondsUntilEndOfDay();

        // 3. 写回缓存（所有线程都可能走到这里！）
        RedisUtils.setValue(redisTemplate, key, exists, expireSecs); // 直接传秒数
        return exists;
    }

    @Override
    public CheckinHistoryResponse getRecentCheckinHistory(long currentUserId, int days) {


        log.info("开始获取最近签到记录: userId={}, days={}", currentUserId, days);
        if (days <= 0 || days > 90) {
            days = 30; // 安全兜底
        }


        LocalDate startDate = LocalDate.now().minusDays(days - 1); // 包含今天共 days 天
        LocalDate endDate = LocalDate.now();

        List<LocalDate> dates = baseMapper.selectCheckinDatesByUserAndDateRange(currentUserId, startDate, endDate);

        // 转为 "yyyy-MM-dd" 字符串列表
        List<String> dateStrings = dates.stream()
                .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .collect(Collectors.toList());

        return new CheckinHistoryResponse(dateStrings);
    }


    /**
     * 计算连续签到天数
     *
     * @param userId      用户ID
     * @param checkinDate 签到日期
     * @param streakKey   连续签到信息Redis键
     * @return 连续签到天数
     */
    private Integer calculateStreakDays(Long userId, LocalDate checkinDate, String streakKey) {
        // 获取上次签到日期
        Map<Object, Object> streakInfo = redisTemplate.opsForHash().entries(streakKey);

        String lastCheckinStr = (String) streakInfo.get("last_checkin_date");


        //首次签到直接设 streak=1，不查 DB。
        int streakDays = 1;
        if (lastCheckinStr == null) {
            // 如果没有上次签到记录，可能是第一次签到 todo 可能存在新设备登录的问题
            // 检查数据库中是否有历史签到记录
//            QueryWrapper<CheckinRecord> queryWrapper = new QueryWrapper<>();
//            queryWrapper.eq("user_id", userId);
//            queryWrapper.orderByDesc("checkin_date");
//            queryWrapper.last("LIMIT 1");
//
//            CheckinRecord lastRecord = this.getOne(queryWrapper);
//            if (lastRecord != null) {
//                lastCheckinStr = lastRecord.getCheckinDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//            }
            streakDays = 1;
        } else {
            LocalDate lastCheckinDate = LocalDate.parse(lastCheckinStr);
            long daysBetween = ChronoUnit.DAYS.between(lastCheckinDate, checkinDate);

            if (daysBetween == 1) {
                // 昨天签到了，连续签到天数+1
                String streakStr = (String) streakInfo.get("current_streak");
                streakDays = streakStr != null ? Integer.parseInt(streakStr) + 1 : 2;
            } else if (daysBetween == 0) {
                // 今天已经签到（理论上不会走到这里，因为前面已经检查过了）
                String streakStr = (String) streakInfo.get("current_streak");
                streakDays = streakStr != null ? Integer.parseInt(streakStr) : 1;
            } else {
                // 中断了连续签到，重置为1
                streakDays = 1;
            }
        }

        // 更新Redis Hash中的连续签到天数和上次签到日期
        Map<String, String> updateValues = new HashMap<>();
        updateValues.put("current_streak", String.valueOf(streakDays));
        updateValues.put("last_checkin_date", checkinDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        redisTemplate.opsForHash().putAll(streakKey, updateValues);

        return streakDays;
    }

    /**
     * 解析等级名称中的数字
     *
     * @param levelName 等级名称，如"LEVEL1"
     * @return 等级数字，如1
     */
    private int parseLevelNumber(String levelName) {
        if (levelName != null && levelName.startsWith("LEVEL")) {
            try {
                return Integer.parseInt(levelName.substring(5));
            } catch (NumberFormatException e) {
                // 如果解析失败，返回默认值1
                return 1;
            }
        }
        return 1;
    }

}