package cn.loblok.upc.service.assist;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

/**
 * 签到奖励规则服务类
 * 根据签到日期返回对应的积分、经验等奖励配置
 */
@Service
public class CheckinRewardRuleService {

    /**
     * 奖励映射表，key 为星期几（0=周一, 1=周二, ..., 6=周日）
     */
    private static final Map<Integer, RewardConfig> REWARD_MAP = Map.of(
            0, new RewardConfig(10, 5, false, null),                    // 周一
            1, new RewardConfig(10, 5, false, null),                    // 周二
            2, new RewardConfig(20, 10, true, "10% 优惠券"),            // 周三
            3, new RewardConfig(50, 15, false, null),                   // 周四
            4, new RewardConfig(15, 5, false, null),                    // 周五
            5, new RewardConfig(15, 5, false, null),                    // 周六
            6, new RewardConfig(100, 50, true, "神秘盲盒")              // 周日
    );

    /**
     * 根据签到日期获取当天的奖励配置
     *
     * @param checkinDate 签到日期（LocalDate）
     * @return 奖励配置对象
     */
    public RewardConfig getRewardByDate(LocalDate checkinDate) {
        DayOfWeek dayOfWeek = checkinDate.getDayOfWeek();
        // Java 中 DayOfWeek.MONDAY = 1, SUNDAY = 7，需转为 0~6
        int index = dayOfWeek.getValue() - 1;
        return REWARD_MAP.get(index);
    }

    /**
     * 奖励配置数据类
     */
    public static class RewardConfig {
        private final int points;
        private final int exp;
        private final boolean isMilestone;
        private final String specialReward;

        public RewardConfig(int points, int exp, boolean isMilestone, String specialReward) {
            this.points = points;
            this.exp = exp;
            this.isMilestone = isMilestone;
            this.specialReward = specialReward;
        }

        // Getters
        public int getPoints() {
            return points;
        }

        public int getExp() {
            return exp;
        }

        public boolean isMilestone() {
            return isMilestone;
        }

        public String getSpecialReward() {
            return specialReward;
        }
    }
}