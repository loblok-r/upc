package cn.loblok.upc.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class CacheUtils {


    private static final ZoneId BUSINESS_TIMEZONE = ZoneId.of("Asia/Shanghai");
    /**
     * 获取今天剩余的秒数（用于缓存今日状态类数据）
     */
    public static long getSecondsUntilEndOfDay() {
        LocalDateTime now = LocalDateTime.now(BUSINESS_TIMEZONE);
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);
        return Math.max(1, ChronoUnit.SECONDS.between(now, endOfDay));
    }


}