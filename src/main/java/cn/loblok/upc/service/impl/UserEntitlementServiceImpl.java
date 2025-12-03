package cn.loblok.upc.service.impl;

import cn.loblok.upc.entity.User;
import cn.loblok.upc.service.DailyQuotaService;
import cn.loblok.upc.service.UserEntitlementService;
import cn.loblok.upc.service.UserService;
import cn.loblok.upc.util.CaculateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

        // 3.会员优先：直接走会员额度
        if (isMember(user)) {
            return checkMemberQuota(user, plan);
        }

        int level = CaculateUtils.getLevelNumber(user.getUserLevel());
        // 4. 定义各等级的权限和日常额度
        switch (upperPlan) {
            case "BASIC":
                // LV1 及以上可使用 BASIC
                if (level < 1) {
                    return false;
                }
                // 检查今日额度是否用完
                int basicDailyQuota = 10; // LV1: 10次/天
                if (level >= 2) {
                    basicDailyQuota = 15; // LV2-LV5: 15次/天
                }
                int basicUsed = dailyQuotaService.getUsedToday(userId, "BASIC");
                return basicUsed < basicDailyQuota;

            case "HD":
                // LV3 及以上可使用 HD
                if (level < 3) {
                    return false;
                }
                // 检查今日额度是否用完
                int hdDailyQuota = 0;
                if (level == 3) {
                    hdDailyQuota = 5;  // LV3: 5次/天
                } else if (level >= 4) {
                    hdDailyQuota = 10; // LV4-LV5: 10次/天
                }
                int hdUsed = dailyQuotaService.getUsedToday(userId, "HD");
                return hdUsed < hdDailyQuota;

            case "PRO":
                // 仅 LV5 可使用 PRO
                if (level < 5) {
                    return false;
                }
                // 检查今日额度
                int proDailyQuota = 5;  // LV5: 5次/天
                int proUsed = dailyQuotaService.getUsedToday(userId, "PRO");
                return proUsed < proDailyQuota;

            default:
                return false;
        }
    }

    @Override
    public void consumeQuota(Long userId, String plan) {
        if (userId == null || plan == null) {
            log.warn("consumeQuota called with invalid args: userId={}, plan={}", userId, plan);
            return;
        }

        String upperPlan = plan.toUpperCase();
        if (!Arrays.asList("BASIC", "HD", "PRO").contains(upperPlan)) {
            log.warn("Invalid plan for consume: {}", plan);
            return;
        }

        // 直接递增使用次数（Redis Hash 已保证原子性）
        dailyQuotaService.incrementUsed(userId, upperPlan);

        log.debug("Consumed quota: userId={}, plan={}", userId, upperPlan);
    }

    @Override
    public boolean isMember(User user) {
        if (user == null) return false;

        // 永久会员
        if (Boolean.TRUE.equals(user.getIsPermanentMember())) {
            return true;
        }

        // 月度会员：检查是否未过期
        if (user.getMemberExpireAt() != null) {
            return user.getMemberExpireAt().isAfter(LocalDateTime.now());
        }

        return false;
    }

    private boolean checkMemberQuota(User user, String plan) {
        String upperPlan = plan.toUpperCase();
        switch (upperPlan) {
            case "BASIC":
                return true; // 会员 BASIC 无限
            case "HD":
                int hdUsed = dailyQuotaService.getUsedToday(user.getId(), "HD");
                return hdUsed < 100; // 会员 HD 100次/天
            case "PRO":
                int proUsed = dailyQuotaService.getUsedToday(user.getId(), "PRO");
                return proUsed < 30; // 会员 PRO 30次/天
            default:
                return false;
        }
    }
}