package cn.loblok.upc.common.enums;


import java.util.function.IntFunction;

/**
 * 功能类别枚举
 */

public enum PlanCategory {
    BASIC("Basic", 1, level -> level >= 2 ? 15 : 10),
    HD("HD", 3, level -> level >= 4 ? 10 : 5),
    PRO("PRO", 5, level -> 5);

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