package cn.loblok.upc.auth.service.chickin.impl;

import cn.loblok.upc.auth.dto.chickin.RewardResult;
import cn.loblok.upc.auth.service.chickin.CheckinRecordService;
import cn.loblok.upc.auth.service.chickin.assist.CheckinManager;
import cn.loblok.upc.auth.service.chickin.assist.CheckinNotifyService;
import cn.loblok.upc.auth.service.UserItemsService;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.auth.entity.chickin.CheckinRecord;
import cn.loblok.upc.auth.mapper.CheckinRecordMapper;
import cn.loblok.upc.auth.dto.ValidationResult;
import cn.loblok.upc.auth.service.UserService;
import cn.loblok.upc.auth.dto.chickin.CheckinHistoryResponse;
import cn.loblok.upc.auth.dto.chickin.CheckinResponseDTO;
import cn.loblok.upc.auth.entity.User;
import cn.loblok.upc.auth.common.util.CacheUtils;
import cn.loblok.upc.common.exception.BusinessException;
import cn.loblok.upc.common.utils.KeyUtils;
import cn.loblok.upc.common.utils.RedisUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.loblok.upc.auth.service.chickin.assist.CheckinRewardCalculator;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
    private final CheckinRewardCalculator rewardCalculator;
    private final StringRedisTemplate redisTemplate;
    private final UserItemsService userItemsService;
    private final CheckinStreakService checkinStreakService;
    private final CheckinManager checkinManager;
    private final CheckinNotifyService checkinNotifyService;
    private static final ZoneId BUSINESS_TIMEZONE = ZoneId.of("Asia/Shanghai");

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<CheckinResponseDTO> checkin(String tenantId, Long userId) {

        return executeCheckin(tenantId, userId, LocalDate.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<CheckinResponseDTO> executeCheckin(String tenantId, Long userId, LocalDate checkinDate) {


        log.info("开始处理签到请求: tenantId={}, userId={},chickinDate = {}", tenantId, userId, checkinDate);

        // 1. 基础校验
        User user = validateUserAndStatus(tenantId, userId, checkinDate);

        // 计算连续签到天数
        int streakDays = checkinStreakService.updateAndGetStreak(userId, checkinDate);

        // 3. 计算奖励
        RewardResult rewards = rewardCalculator.calculate(user,checkinDate, streakDays);

        // 4. 持久化 DB (Record, UserPoints, User)
        long recordId = checkinManager.persistCheckinData(tenantId, user, checkinDate, rewards, streakDays);

        // 5. 更新缓存与异步流水
        checkinNotifyService.syncCacheAndNotify(tenantId, userId, recordId, rewards);

        return Result.success(new CheckinResponseDTO(rewards.getFinalTotalPoints(), streakDays));
    }
    /**
     * 基础校验
     */
    private User validateUserAndStatus(String tenantId, Long userId, LocalDate checkinDate) {
        String bizKey = KeyUtils.buildCheckinBizKey(tenantId, userId, checkinDate.toString());
        // 获取用户信息
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 获取当前日期
        String dateStr = checkinDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // 检查是否已经签到
        QueryWrapper<CheckinRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("biz_key", bizKey);
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException("今天已经签到过了");
        }
        return user;
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
            boolean deductSuccess = userItemsService.deductRetroCard(userId);
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



    @Override
    public boolean hasCheckedInToday(String tenantId, Long userId) {
        return hasCheckedInToday(tenantId, userId, LocalDate.now());
    }

    public boolean hasCheckedInToday(String tenantId, Long userId, LocalDate checkinDate) {
        log.info("开始处理查询今天是否已签到请求: tenantId={}, userId={}", tenantId, userId);
        String key = KeyUtils.buildCheckinStatusKey(userId) + LocalDate.now(BUSINESS_TIMEZONE);

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

}