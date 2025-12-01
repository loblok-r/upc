package cn.loblok.upc.enums;

//积分类型
public enum BizType {

    checkin_daily("日常签到"),
    checkin_streak_bonus("连续签到奖励");

    private final String desc;
    BizType(String desc) { this.desc = desc; }
    public String getDesc() { return desc; }

}
