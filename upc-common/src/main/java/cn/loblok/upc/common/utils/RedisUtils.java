package cn.loblok.upc.common.utils;


import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class RedisUtils {

    // Redis key 前缀
    private static final String USER_SCORE_KEY_PREFIX = "user_score_";
    private static final String USER_STREAK_KEY_PREFIX = "user_streak_";

    private static final String USER_LEVEL_KEY_PREFIX = "user:level:";

    private static final String USER_EXP_KEY_PREFIX = "user:exp:";

    private static final String USER_DAILY_QUATA_PREFIX = "user:daily:quata:";


    private static final String TODAY_CHECKIN_STATUS_KEY_PREFIX = "checkin:status:";

    private static final String Coupon_STOCK_PREFIX ="coupon:stock:";

    private static final String USER_VERIFICATION_KEY_PREFIX ="user:verification-code:";



    public static String buildDailyQuotaKey(Long userId){
        String dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return USER_DAILY_QUATA_PREFIX + userId + ":" + dateStr;
    }
    public static String buildPointsKey(Long userId) {
        return USER_SCORE_KEY_PREFIX + userId;
    }

    public static String buildStreakKey(Long userId) {
        return USER_STREAK_KEY_PREFIX + userId;
    }

    public static String buildLevelKey(Long userId) {
        return USER_LEVEL_KEY_PREFIX + userId;
    }

    public static String buildExpKey(Long userId) {
        return USER_EXP_KEY_PREFIX + userId;
    }



    public static String buildCouponStockKey(Long templateId) {
        return Coupon_STOCK_PREFIX + templateId;
    }
    public static String buildCheckinStatusKey(Long userId) {
        return TODAY_CHECKIN_STATUS_KEY_PREFIX + userId;
    }

    public static String buildVerificationCodeKey(String email, String type) {
        return USER_VERIFICATION_KEY_PREFIX +type + ":"+ email;
    }

    // 支持 StringRedisTemplate
    public static void setValue(StringRedisTemplate redisTemplate,
                                String key, boolean value, Duration expire) {
        redisTemplate.opsForValue().set(key, String.valueOf(value),
                expire.toMillis(), TimeUnit.MILLISECONDS);
    }

    public static void setValue(StringRedisTemplate redisTemplate,
                                String key, boolean value, long expireSeconds) {
        redisTemplate.opsForValue().set(key, String.valueOf(value), expireSeconds, TimeUnit.SECONDS);
    }

    // 或者更通用：支持任意 value（转成 String）
    public static void setValue(StringRedisTemplate redisTemplate,
                                String key, Object value, Duration expire) {
        redisTemplate.opsForValue().set(key, String.valueOf(value),
                expire.toMillis(), TimeUnit.MILLISECONDS);
    }

}