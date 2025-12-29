package cn.loblok.upc.auth.dto.chickin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 签到奖励计算结果对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RewardResult {

    // --- 积分相关 ---
    private int basePoints;      // 基础签到积分（如：10分）
    private int bonusPoints;     // 连签额外奖励积分（如：30分）
    private int totalPoints;     // 本次获得的总积分 (basePoints + bonusPoints)
    private int finalTotalPoints; // 加上本次奖励后，用户的实时总积分余额

    // --- 经验相关 ---
    private int addedExp;        // 本次获得的经验值
    private int newTotalExp;    // 加上本次奖励后，用户的总经验值

    // --- 等级相关 ---
    private String oldLevel;     // 签到前的等级名称 (如: LEVEL1)
    private String newLevel;     // 签到后的等级名称 (如: LEVEL2)

    // --- 状态标记 ---
    private boolean hasBonus;    // 是否触发了连签奖励
    private boolean levelUpgraded; // 是否升级

    /**
     * 判断是否升级的便捷方法
     */
    public boolean isLevelUpgraded() {
        return oldLevel != null && !oldLevel.equals(newLevel);
    }
}