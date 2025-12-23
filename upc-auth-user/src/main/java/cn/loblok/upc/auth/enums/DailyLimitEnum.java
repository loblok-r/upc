package cn.loblok.upc.auth.enums;

import lombok.Getter;

@Getter
public enum DailyLimitEnum {
    TEXT_CHAT(5),
    AI_DRAWING(3),
    Default(10);

    private final int limit;

    DailyLimitEnum(int limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }
}