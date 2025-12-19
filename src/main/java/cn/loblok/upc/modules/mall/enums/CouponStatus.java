package cn.loblok.upc.modules.mall.enums;

import lombok.Getter;

/**
 * 优惠券状态枚举
 */
@Getter
public enum CouponStatus {
    
    UNUSED((byte) 0, "未使用"),
    USED((byte) 1, "已使用"),
    EXPIRED((byte) 2, "已过期");
    
    private final Byte value;
    private final String description;
    
    CouponStatus(byte value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public static CouponStatus fromValue(byte value) {
        for (CouponStatus status : CouponStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的优惠券状态值: " + value);
    }
}