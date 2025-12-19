package cn.loblok.upc.modules.member.enums;


import java.util.function.IntFunction;

/**
 * 功能类别枚举
 */

public enum PlanCategory {
    BASIC("Basic", 1, level -> level >= 2 ? 15 : 10),
    HD("HD", 1, level -> {
        if (level < 3) return 0;  // 3级以下不能使用
        if (level == 5) return 10; // 5级每天用10次
        return 5;                  // 3-4级每天用5次
    }),
    PRO("PRO", 1, level -> 0); // PRO功能 只有会员能用

    private final String name;
    private final int minLevel;
    private final IntFunction<Integer> quotaCalculator;

    PlanCategory(String name, int minLevel, IntFunction<Integer> quotaCalculator) {
        this.name = name;
        this.minLevel = minLevel;
        this.quotaCalculator = quotaCalculator;
    }

    public String getName() {
        return name;
    }

    public boolean hasPermission(int userLevel) {
        return userLevel >= minLevel;
    }

    public int getDailyQuota(int userLevel) {
        return quotaCalculator.apply(userLevel);
    }

    public static PlanCategory fromName(String name) {
        for (PlanCategory category : values()) {
            if (category.name.equalsIgnoreCase(name)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid plan category: " + name);
    }
}