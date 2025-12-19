package cn.loblok.upc.modules.member.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 会员订单状态枚举
 */
public enum MembershipOrderStatus {

    CREATED("CREATED"),//创建
    PAYING("PAYING"), // 支付中
    CLOSED("CLOSED"), //超时未支付 ，订单关闭
    FAILED("FAILED"),//支付失败
    PAID("PAID"), //支付成功
    REFUNDED("REFUNDED");

    @EnumValue
    private final String value;

    MembershipOrderStatus(String value) {
        this.value = value;
    }


    public String getValue() {
        return this.name();
    }

    public boolean canTransitionTo(MembershipOrderStatus target) {
        return switch (this) {
            case CREATED ->
                    target == PAYING || target == CLOSED; // ✅ 允许超时关闭
            case PAYING ->
                    target == PAID || target == FAILED || target == CLOSED; // ✅ 支付中可超时关闭
            case PAID ->
                    target == REFUNDED;
            case CLOSED, FAILED, REFUNDED ->
                    false; // 终态不可变
        };
    }

    // 定义是否为终态
    public boolean isFinal() {
        return this == CLOSED ||
                this == FAILED ||
                this == REFUNDED;
    }

    public static MembershipOrderStatus fromValue(String value) {
        for (MembershipOrderStatus status : values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid order status: " + value);
    }
}
