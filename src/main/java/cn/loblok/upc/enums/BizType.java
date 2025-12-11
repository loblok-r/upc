package cn.loblok.upc.enums;

/**
 * 业务类型枚举
 */
public enum BizType {

    REGISTER_GIFT("注册礼包"),

    //INVITE_REWARD("邀请奖励"),
   // MARKETING_CAMPAIGN("营销活动"),
    UPGRADE_PACKAGE("升级礼包"),
    //ACTIVITY_PARTICIPATION("活动参与奖励"),
    TASK_REWARD("任务奖励"),

    DAILY_SIGN("每日签到"),
    STREAK_SIGN("连续签到");

    private final String desc;


    BizType(String desc) { this.desc = desc; }
    public String getDesc() { return desc; }

    public static BizType fromName(String name) {
        for (BizType value : values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return null;
    }

}
