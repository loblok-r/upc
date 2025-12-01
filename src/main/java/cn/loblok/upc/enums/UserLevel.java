package cn.loblok.upc.enums;

import lombok.Data;

// 1. 等级枚举（清晰表达业务规则）

public enum UserLevel {
    LEVEL1(0, "Level1"),
    LEVEL2(500, "Level2"),
    LEVEL3(1000, "Level3"),
    LEVEL4(2000, "Level4"),
    LEVEL5(5000, "Level5"),;

    private final int minPoints;

    UserLevel(int minPoints, String desc) {
        this.minPoints = minPoints;
    }

    public static String getDesc(int points) {
        // 从高到低检查
        for (int i = UserLevel.values().length - 1; i >= 0; i--) {
            UserLevel level = UserLevel.values()[i];
            if (points >= level.minPoints) {
                return level.name();
            }
        }
        return "LEVEL1"; // 默认
    }
}
