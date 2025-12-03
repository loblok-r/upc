package cn.loblok.upc.common.enums;

import lombok.Getter;

/**
 * 优惠券模板类型枚举
 */
@Getter
public enum CouponTemplateType {
    
    // 新用户注册类
    WELCOME_2025("WELCOME_2025", "新用户注册", "首单满减"),

    HD_One_Time_Experience("HD_One_Time_Experience", "HD体验券", "解锁一次使用HD使用功能"),

    PRO_One_Time_Experience("PRO_One_Time_Experience", "PRO体验券", "解锁一次使用PRO使用功能"),

    MEMBER_ONE_DAY("MEMBER_ONE_DAY", "一天会员体验权益", "一天会员体验权益");

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