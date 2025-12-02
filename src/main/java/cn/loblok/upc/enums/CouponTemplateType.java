package cn.loblok.upc.enums;

import lombok.Getter;

/**
 * 优惠券模板类型枚举
 */
@Getter
public enum CouponTemplateType {
    
    // 新用户注册类
    WELCOME_2025("WELCOME_2025", "新用户注册", "通常有使用门槛（如首单满减）"),
    
    // AI服务试用类
    AI_TRIAL_7D("AI_TRIAL_7D", "AI服务试用", "免费试用7天，到期自动失效"),
    
    // AI套餐类
    AI_BASIC_M("AI_BASIC_M", "AI基础套餐", "月付订阅券"),
    AI_PRO_Y("AI_PRO_Y", "AI高级套餐", "年付折扣券"),
    
    // 签到类
    SIGN_DAILY("SIGN_DAILY", "每日签到", "每天可领，短期有效"),
    
    // 邀请奖励类
    INVITE_REWARD("INVITE_REWARD", "邀请奖励", "邀请成功后发放"),
    
    // 节日营销类
    XMAS_2025("XMAS_2025", "圣诞节活动", "圣诞限时活动"),

    LEVEL_2_WELCOME("LEVEL_2_WELCOME", "升2级奖励", "升级奖励"),

    LEVEL_3_ELITE("LEVEL_3_ELITE", "升3级奖励", "升级奖励"),

    LEVEL_5_ELITE("LEVEL_5_ELITE", "升5级奖励", "升级奖励"),

    BF_2025("BF_2025", "黑色星期五", "黑五限时活动"),
    
    // 流失召回类
    COME_BACK_30("COME_BACK_30", "流失召回", "针对沉默用户发放"),
    
    // 订单满减类
    ORDER_100_OFF_10("ORDER_100_OFF_10", "订单满减", "满100减10（这类通常动态生成，不一定放枚举）");
    
    private final String templateCode;
    private final String typeName;
    private final String description;
    
    CouponTemplateType(String templateCode, String typeName, String description) {
        this.templateCode = templateCode;
        this.typeName = typeName;
        this.description = description;
    }
    
    @Override
    public String toString() {
        return this.templateCode;
    }
}