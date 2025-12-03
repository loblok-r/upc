package cn.loblok.upc.enums;


// 1. 等级枚举（清晰表达业务规则）

public enum UserLevel {
    LEVEL1(0, "Level1"),
    LEVEL2(500, "Level2"),
    LEVEL3(1000, "Level3"),
    LEVEL4(2000, "Level4"),
    LEVEL5(5000, "Level5"),;

    private final int minExps;

    UserLevel(int minExps, String desc) {
        this.minExps = minExps;
    }

    public static String getDesc(int exps) {
        UserLevel result = LEVEL1;
        for (UserLevel level : UserLevel.values()) {
            if (exps >= level.minExps && level.minExps > result.minExps) {
                result = level;
            }
        }
        return result.name();
    }
    
    /**
     * 将等级名称映射为数字
     * @param levelName 等级名称，如"LEVEL1"
     * @return 等级数字，如1；如果无法解析则返回0
     */
    public static int getLevelNumber(String levelName) {
        if (levelName != null && levelName.startsWith("LEVEL")) {
            try {
                return Integer.parseInt(levelName.substring(5));
            } catch (NumberFormatException e) {
                // 如果解析失败，返回默认值0
                return 0;
            }
        }
        return 0;
    }
}