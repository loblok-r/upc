package cn.loblok.upc.common.util;

public class PrefixUtil {

    private static final String REGISTER_COUPON_ISSUE_PREFIX ="REGISTER_GIFT_";

    private static final String USER_LEVEL_UP_COUPON_ISSUE_PREFIX ="UPGRADE_LVL_GIFT_";

    public static String buildRegisterCouponIssueKey(Long userID) {
        return REGISTER_COUPON_ISSUE_PREFIX + userID;
    }

    public static String buildLevelUpgradeCouponKey(Long userId,int  level) {
        return USER_LEVEL_UP_COUPON_ISSUE_PREFIX + level + "_" + userId;
    }



}