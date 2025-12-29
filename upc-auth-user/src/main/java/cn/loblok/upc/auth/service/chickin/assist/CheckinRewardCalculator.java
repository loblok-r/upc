package cn.loblok.upc.auth.service.chickin.assist;

import cn.loblok.upc.auth.dto.chickin.RewardResult;
import cn.loblok.upc.auth.entity.User;
import cn.loblok.upc.auth.common.util.CaculateUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@AllArgsConstructor
public class CheckinRewardCalculator {

    private final CheckinRewardRuleService rewardRuleService;
    private final CaculateUtils caculateUtils;

    // 可以将配置项放入数据库或 Apollo 配置中心，这里作为常量示例
    private static final int STREAK_THRESHOLD = 7;
    private static final int STREAK_BONUS = 30;

    /**
     * 核心计算方法：根据用户信息和签到情况，计算完整的奖励结果
     */
    public RewardResult calculate(User user, LocalDate date, int streakDays) {
        // 1. 获取配置的基础奖励 (基础积分和经验)
        CheckinRewardRuleService.RewardConfig daily = rewardRuleService.getRewardByDate(date);
        int basePoints = daily.getPoints();
        int addedExp = daily.getExp();

        // 2. 连签奖励逻辑判断
        int bonusPoints = 0;
        boolean hasBonus = false;
        if (streakDays >= STREAK_THRESHOLD && streakDays % STREAK_THRESHOLD == 0) {
            bonusPoints = STREAK_BONUS;
            hasBonus = true;
        }

        // 3. 累计值计算
        int totalPoints = basePoints + bonusPoints;
        int finalTotalPoints = user.getPoints() + totalPoints;
        int newTotalExp = user.getExp() + addedExp;

        // 4. 等级变更计算
        String oldLevel = user.getUserLevel();
        String newLevel = caculateUtils.calculateLevel(newTotalExp);
        boolean levelUpgraded = oldLevel != null && !oldLevel.equals(newLevel);

        // 5. 使用 Builder 模式构建结果对象
        return RewardResult.builder()
                .basePoints(basePoints)
                .bonusPoints(bonusPoints)
                .totalPoints(totalPoints)
                .finalTotalPoints(finalTotalPoints)
                .addedExp(addedExp)
                .newTotalExp(newTotalExp)
                .oldLevel(oldLevel)
                .newLevel(newLevel)
                .hasBonus(hasBonus)
                .levelUpgraded(levelUpgraded)
                .build();
    }
}