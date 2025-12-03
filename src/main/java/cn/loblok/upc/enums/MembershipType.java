package cn.loblok.upc.enums;

import lombok.Getter;
import java.math.BigDecimal;

/**
 * 会员类型枚举
 */
@Getter
public enum MembershipType {
    
    MONTHLY("MONTHLY", "月度会员", new BigDecimal("19.90"), 30),
    PERMANENT("PERMANENT", "永久会员", new BigDecimal("199.00"), -1);
    
    private final String code;
    private final String description;
    private final BigDecimal price;
    private final int durationDays;
    
    MembershipType(String code, String description, BigDecimal price, int durationDays) {
        this.code = code;
        this.description = description;
        this.price = price;
        this.durationDays = durationDays;
    }
    
    public static MembershipType fromCode(String code) {
        for (MembershipType type : MembershipType.values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("不支持的会员类型: " + code);
    }
}