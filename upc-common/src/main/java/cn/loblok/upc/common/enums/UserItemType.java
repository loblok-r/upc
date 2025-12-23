package cn.loblok.upc.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum UserItemType {


    COMPUTE_POWER(1, "compute_power"),
    VIP(2, "vip"),
    RESIGN_CARD(3, "resign_card"),
    LOTTERY_TICKET(4, "lottery_ticket"),
    CASH_BONUS(5, "cash_bonus"),
    AI_DRAWING_QUOTA(6, "ai_drawing_quota"),
    ANNUAL_MEMBERSHIP_VOUCHER(7, "annual_membership_voucher"),
    DISCOUNT_COUPON(8, "discount_coupon");

    @EnumValue
    private final Integer code;

    private final String value;


    UserItemType(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    public Integer getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}
