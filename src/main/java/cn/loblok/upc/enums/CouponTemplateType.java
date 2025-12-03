package cn.loblok.upc.enums;

import lombok.Getter;

/**
 * 优惠券模板类型枚举
 */
@Getter
public enum CouponTemplateType {
    
    // 新用户注册类
    WELCOME_2025("WELCOME_2025", "新用户注册", "通常有使用门槛（如首单满减）"),

    LEVEL_3_ELITE("LEVEL_3_ELITE", "升3级奖励", "升级奖励"),

    LEVEL_5_ELITE("LEVEL_5_ELITE", "升5级奖励", "升级奖励"),

    ;
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