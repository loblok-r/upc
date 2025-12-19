package cn.loblok.upc.modules.ai.enums;

import lombok.Getter;

@Getter
public enum AllowHistoryDaysEnum {
    
    VIP(30),
    COMMON(7);
    
    private final int days;

    AllowHistoryDaysEnum(int days) {
        this.days = days;
    }
    
    public int getDays() {
        return days;
    }
}