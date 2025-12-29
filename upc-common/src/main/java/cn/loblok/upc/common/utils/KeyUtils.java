package cn.loblok.upc.common.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class KeyUtils {

    private static final String USER_SCORE_KEY_PREFIX = "user_score_";
    private static final String USER_EXP_KEY_PREFIX = "user:exp:";
    private static final String USER_STREAK_KEY_PREFIX = "user_streak_";
    private static final String USER_LEVEL_KEY_PREFIX = "user:level:";
    private static final String USER_VERIFICATION_KEY_PREFIX = "user:verification-code:";
    private static final String USER_DAILY_QUATA_PREFIX = "user:daily:quata:";



    private static final String CHECKIN_BIZKEY_PREFIX = "checkin_";
    private static final String TODAY_CHECKIN_STATUS_KEY_PREFIX = "checkin:status:";


    private static final String FLASHSALE_STOCK_PREFIX = "flash:stock:";
    private static final String FLASHSALE_USER_PREFIX = "flash:user:";
    private static final String FLASHSALE_LOCK_PREFIX = "flashsale:lock:";
    private static final String PRODUCT_STOCK_PREFIX = "product:stock:";


    private static final String Coupon_STOCK_PREFIX = "coupon:stock:";
    private static final String COMMUNITY_LEADERBOARD_CREATORS = "community:leaderboard:creators";



    /**
     * 签到业务 key
     *
     * @param tenantId
     * @param userId
     * @param dateStr
     * @return
     */
    public static String buildCheckinBizKey( String tenantId, Long userId, String dateStr) {
        return CHECKIN_BIZKEY_PREFIX +  tenantId + "_" + userId + "_" + dateStr;
    }

    /**
     * 用户积分 key
     *
     * @param userId
     * @return
     */
    public static String buildPointsKey(Long userId) {
        return USER_SCORE_KEY_PREFIX + userId;
    }

    /**
     * 秒杀用户 key
     *
     * @param userId
     * @param flashSaleId
     * @return
     */
    public static String buildFlashSaleUserKey(Long userId, String flashSaleId) {
        return FLASHSALE_USER_PREFIX + userId + ":" + flashSaleId;
    }


    /**
     * 秒杀库存 key
     *
     * @param flashSaleId
     * @return
     */
    public static String buildFlashSaleStockKey(String flashSaleId) {
        return FLASHSALE_STOCK_PREFIX + flashSaleId;
    }

    /**
     * <p>
     * /**
     * 秒杀锁 key
     *
     * @param userId
     * @param flashSaleId
     * @return
     */
    public static String buildFlashSaleLockKey(Long userId, String flashSaleId) {
        return FLASHSALE_LOCK_PREFIX + userId + ":" + flashSaleId;
    }

    /**
     * 商品库存 key
     *
     * @param productId
     * @return
     */
    public static String buildProductStockKey(String productId) {
        return PRODUCT_STOCK_PREFIX + productId;
    }


    /**
     * 连续签到 key
     **/
    public static String buildStreakKey(Long userId) {
        return USER_STREAK_KEY_PREFIX + userId;
    }

    /**
     * 签到等级 key
     **/
    public static String buildLevelKey(Long userId) {
        return USER_LEVEL_KEY_PREFIX + userId;
    }

    public static String buildExpKey(Long userId) {
        return USER_EXP_KEY_PREFIX + userId;
    }


    /**
     * 优惠券库存 key
     **/
    public static String buildCouponStockKey(Long templateId) {
        return Coupon_STOCK_PREFIX + templateId;
    }

    /**
     * 签到状态 key
     **/
    public static String buildCheckinStatusKey(Long userId) {
        return TODAY_CHECKIN_STATUS_KEY_PREFIX + userId;
    }

    /**
     * 验证码 key
     **/
    public static String buildVerificationCodeKey(String email, String type) {
        return USER_VERIFICATION_KEY_PREFIX + type + ":" + email;
    }


    /**
     * 每日签到 key
     **/
    public static String buildDailyQuotaKey(Long userId) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return USER_DAILY_QUATA_PREFIX + userId + ":" + dateStr;
    }

    /**
     * 社区排行榜 key
     **/
    public static String buildCommunityLeaderboardCreatorsKey() {
        return COMMUNITY_LEADERBOARD_CREATORS;
    }

}