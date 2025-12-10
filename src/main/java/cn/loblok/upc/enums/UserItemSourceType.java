package cn.loblok.upc.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum UserItemSourceType {

    // 积分秒杀
    FLASH_SALE(1, "积分秒杀"),

    // 积分兑换
    POINTS_TRADING(2, "积分兑换"),

    // 每日任务
    DAILY_TASK(3, "每日任务"),

    // 活动奖励
    EVENT_REWARD(4, "活动奖励"),

    // 运营发放
    ADMIN_GRANT(5, "运营发放");

    @EnumValue
    private final int code;
    private final String description;

    UserItemSourceType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}