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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private UserService userService;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private LeaderboardServiceImpl leaderboardService;

    @Autowired
    private  PointTransactionService pointTransactionService;
    
    // Redis key 前缀
    private static final String USER_SCORE_KEY_PREFIX = "user_score_";
    private static final String USER_STREAK_KEY_PREFIX = "user_streak_";

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
        String scoreKey = USER_SCORE_KEY_PREFIX + request.getUserId();
        // 用户连续签到信息Key
        String streakKey = USER_STREAK_KEY_PREFIX + request.getUserId();

        // 检查是否已经签到
        QueryWrapper<CheckinRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("biz_key", bizKey);
        if (this.count(queryWrapper) > 0) {
            Integer points = getPoints(scoreKey);
            Integer streakDays = getStreakDays(streakKey);
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
            Integer points = getPoints(scoreKey);
            Integer streakDays = getStreakDays(streakKey);
            CheckinResponseDTO checkinResponseDTO = new CheckinResponseDTO(points, streakDays);
            return Result.error("签到失败", checkinResponseDTO);
        }

        // 增加用户积分（基础10分）
        Long pointsAfterBase = redisTemplate.opsForValue().increment(scoreKey, 10);

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
        Integer points = getPoints(scoreKey);
        CheckinResponseDTO checkinResponseDTO = new CheckinResponseDTO(points, streakDays);
        return Result.success(checkinResponseDTO);
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

    // 获取最新的积分值
    private Integer getPoints(String scoreKey) {
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
    private Integer getStreakDays(String streakKey) {
        Object streakObj = redisTemplate.opsForHash().get(streakKey, "current_streak");
        String streakStr = streakObj != null ? (String) streakObj : null;
        return streakStr != null ? Integer.parseInt(streakStr) : 0;
    }
}