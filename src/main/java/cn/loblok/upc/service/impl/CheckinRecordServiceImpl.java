package cn.loblok.upc.service.impl;

import cn.loblok.upc.dto.CheckinRequestDTO;
import cn.loblok.upc.dto.CheckinResponseDTO;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.entity.CheckinRecord;
import cn.loblok.upc.entity.User;
import cn.loblok.upc.enums.BizType;
import cn.loblok.upc.mapper.CheckinRecordMapper;
import cn.loblok.upc.service.CheckinRecordService;
import cn.loblok.upc.service.PointTransactionService;
import cn.loblok.upc.service.UserService;
import cn.loblok.upc.util.CacheUtils;
import cn.loblok.upc.util.CaculateUtils;
import cn.loblok.upc.util.RedisUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-11-30
 */
@Service
@Slf4j
public class CheckinRecordServiceImpl extends ServiceImpl<CheckinRecordMapper, CheckinRecord> implements CheckinRecordService {

    public static AtomicInteger dbQueryCount = new AtomicInteger(0);

    @Autowired
    private UserService userService;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private LeaderboardServiceImpl leaderboardService;

    @Autowired
    private  PointTransactionService pointTransactionService;

    @Autowired
    private CaculateUtils caculateUtils;
    


    private static final ZoneId BUSINESS_TIMEZONE = ZoneId.of("Asia/Shanghai");



    
    @Override
    public Result<CheckinResponseDTO> checkin(String tenantId, CheckinRequestDTO request) {
        log.info("开始处理签到请求: tenantId={}, userId={}", tenantId, request.getUserId());

        // 获取用户信息
        User user = userService.getById(request.getUserId());
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 获取当前日期
        LocalDate checkinDate = LocalDate.now(BUSINESS_TIMEZONE);
        String dateStr = checkinDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // 构造 biz_key
        String bizKey = "checkin_" + tenantId + "_" + request.getUserId() + "_" + dateStr;
        // 用户积分Key（使用Redis）
        String scoreKey = RedisUtils.buildScoreKey(user.getId());
        // 用户连续签到信息Key
        String streakKey = RedisUtils.buildStreakKey(user.getId());

        String levelKey = RedisUtils.buildLevelKey(user.getId());

        // 检查是否已经签到
        QueryWrapper<CheckinRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("biz_key", bizKey);
        if (this.count(queryWrapper) > 0) {
            Integer points = caculateUtils.getPoints(scoreKey);
            Integer streakDays = caculateUtils.getStreakDays(streakKey);
            CheckinResponseDTO checkinResponseDTO = new CheckinResponseDTO(points, streakDays);
            return Result.error("今日已签到", checkinResponseDTO);
        }
        
        // 创建签到记录
        CheckinRecord checkinRecord = new CheckinRecord();
        checkinRecord.setUserId(request.getUserId());
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
            return Result.error("签到失败", checkinResponseDTO);
        }

        String cacheKey = RedisUtils.buildCheckinStatusKey(request.getUserId()) + ":" + LocalDate.now(BUSINESS_TIMEZONE);
        long expireSecs = CacheUtils.getSecondsUntilEndOfDay();
        redisTemplate.opsForValue().set(cacheKey, "true", expireSecs, TimeUnit.SECONDS);

        // 增加用户积分（基础10分）
        Long pointsAfterBase = redisTemplate.opsForValue().increment(scoreKey, 10);

        redisTemplate.delete(levelKey);
        // 更新排行榜（基础10分）
        leaderboardService.updateLeaderboardScore(tenantId, request.getUserId(), 10);

        // 异步记录积分流水
        pointTransactionService.asyncLog(
                tenantId, request.getUserId(), BizType.checkin_daily, checkinRecord.getId(), 10, pointsAfterBase
        );
        
        // 计算连续签到天数
        Integer streakDays = calculateStreakDays(request.getUserId(), checkinDate, streakKey);
        
        // 额外奖励：如果连续签到7天，额外增加50积分
        int bonusPoints = 0;
        Long pointsAfterBonus = pointsAfterBase;
        if (streakDays >= 7 && streakDays % 7 == 0) {
            pointsAfterBonus  = redisTemplate.opsForValue().increment(scoreKey, 50);

            redisTemplate.delete(levelKey);

            // 更新排行榜（额外50分）
            leaderboardService.updateLeaderboardScore(tenantId, request.getUserId(), 50);
            // 异步记录积分流水
            pointTransactionService.asyncLog(
                    tenantId, request.getUserId(), BizType.checkin_streak_bonus, checkinRecord.getId(), 50, pointsAfterBonus
            );
            bonusPoints = 50;
        }
        
        // 设置Redis键的过期时间（例如：30天）
        redisTemplate.expire(scoreKey, 30, TimeUnit.DAYS);
        redisTemplate.expire(streakKey, 30, TimeUnit.DAYS);
        
        // 返回成功响应，包含最新的积分值和连续签到天数
        Integer points = caculateUtils.getPoints(scoreKey);
        CheckinResponseDTO checkinResponseDTO = new CheckinResponseDTO(points, streakDays);
        return Result.success(checkinResponseDTO);
    }

    //查询今天是否已经签到了  ⚠️ 先写一个【有问题的版本】用于演示缓存击穿
    @Override
    public boolean hasCheckedInToday(String tenantId, Long userId) {
        log.info("开始处理查询今天是否已签到请求: tenantId={}, userId={}", tenantId, userId);
        String key= RedisUtils.buildCheckinStatusKey(userId) + LocalDate.now(BUSINESS_TIMEZONE);

        // 1. 查缓存
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return Boolean.parseBoolean(cached);
        }

        // 2. 缓存未命中 → 查 DB（击穿点！）
        QueryWrapper<CheckinRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tenant_id", tenantId)
                    .eq("user_id", userId)
                    .eq("checkin_date", LocalDate.now());

        // 👇 关键：记录一次 DB 查询
        dbQueryCount.incrementAndGet();
        boolean exists = this.exists(queryWrapper);

        // 计算今天还剩多少秒
        long expireSecs = CacheUtils.getSecondsUntilEndOfDay();

        // 3. 写回缓存（所有线程都可能走到这里！）
        RedisUtils.setValue(redisTemplate, key, exists, expireSecs); // 直接传秒数
        return exists;
    }

    /**
     * 计算连续签到天数
     * @param userId 用户ID
     * @param checkinDate 签到日期
     * @param streakKey 连续签到信息Redis键
     * @return 连续签到天数
     */
    private Integer calculateStreakDays(Long userId, LocalDate checkinDate, String streakKey) {
        // 获取上次签到日期
        Map<Object, Object> streakInfo = redisTemplate.opsForHash().entries(streakKey);
        
        String lastCheckinStr = (String) streakInfo.get("last_checkin_date");


        //首次签到直接设 streak=1，不查 DB。
        int streakDays = 1;
        if (lastCheckinStr == null) {
            // 如果没有上次签到记录，可能是第一次签到
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
        }else{
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

}