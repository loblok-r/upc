package cn.loblok.upc.auth.service.chickin.impl;


import cn.loblok.upc.common.utils.KeyUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * 签到连击服务类
 */
@Service
@AllArgsConstructor
public class CheckinStreakService {

    private final StringRedisTemplate redisTemplate;

    public int updateAndGetStreak(Long userId, LocalDate checkinDate) {
        String streakKey = KeyUtils.buildStreakKey(userId);
        Map<Object, Object> info = redisTemplate.opsForHash().entries(streakKey);

        String lastDateStr = (String) info.get("last_checkin_date");
        int currentStreak = info.get("current_streak") == null ? 0 : Integer.parseInt((String)info.get("current_streak"));

        int newStreak = 1;
        if (lastDateStr != null) {
            LocalDate lastDate = LocalDate.parse(lastDateStr);
            long gap = ChronoUnit.DAYS.between(lastDate, checkinDate);
            if (gap == 1) {newStreak = currentStreak + 1;}
            else if (gap == 0) {newStreak = currentStreak;}
        }

        Map<String, String> update = new HashMap<>();
        update.put("current_streak", String.valueOf(newStreak));
        update.put("last_checkin_date", checkinDate.toString());
        redisTemplate.opsForHash().putAll(streakKey, update);
        return newStreak;
    }
}