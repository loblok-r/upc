package cn.loblok.upc.service.impl;

import cn.loblok.upc.entity.User;
import cn.loblok.upc.service.DailyQuotaService;
import cn.loblok.upc.service.UserEntitlementService;
import cn.loblok.upc.service.UserService;
import cn.loblok.upc.util.CaculateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Slf4j
public class UserEntitlementServiceImpl implements UserEntitlementService {

    @Autowired
    private DailyQuotaService dailyQuotaService;

    @Autowired
    private UserService userService;

    @Override
    public boolean canUsePlan(Long userId, String plan) {

        // 1. 参数校验
        if (userId == null || plan == null) {
            return false;
        }
        String upperPlan = plan.toUpperCase();
        if (!Arrays.asList("BASIC", "HD", "PRO").contains(upperPlan)) {
            return false;
        }

        // 2. 获取用户信息
        User user = userService.getById(userId);
        if (user == null || user.getUserLevel() == null) {
            return false;
        }
        int level = CaculateUtils.getLevelNumber(user.getUserLevel());

        // 3. 定义各等级的权限和日常额度
        switch (upperPlan) {
            case "BASIC":
                // 所有用户（LV1+）都能用 BASIC
                return level >= 1;

            case "HD":
                // LV2 及以上可使用 HD
                if (level < 2) {
                    return false;
                }
                // 检查今日额度是否用完
                int hdDailyQuota = (level >= 2) ? 3 : 0;          // LV2: 3次
                if (level >= 3) hdDailyQuota = 5;                 // LV3~5: 5次
                int hdUsed = dailyQuotaService.getUsedToday(userId, "HD");
                return hdUsed < hdDailyQuota;

            case "PRO":
                // 仅 LV5 可使用 PRO
                if (level < 5) {
                    return false;
                }
                // 检查今日额度
                int proDailyQuota = 2;  // LV5: 2次/天
                int proUsed = dailyQuotaService.getUsedToday(userId, "PRO");
                return proUsed < proDailyQuota;

            default:
                return false;
        }
    }

    // todo 实现
    @Override
    public void consumeQuota(Long userId, String plan) {

    }
}