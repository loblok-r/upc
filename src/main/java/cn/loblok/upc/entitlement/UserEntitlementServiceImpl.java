package cn.loblok.upc.entitlement;

import cn.loblok.upc.coupon.CouponTemplate;
import cn.loblok.upc.user.User;
import cn.loblok.upc.common.enums.CouponTemplateEntitlementFeature;
import cn.loblok.upc.common.enums.MembershipType;
import cn.loblok.upc.common.enums.PlanCategory;
import cn.loblok.upc.membershiporder.MembershipOrderService;
import cn.loblok.upc.user.UserService;
import cn.loblok.upc.common.util.CaculateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@Primary
@Slf4j
public class UserEntitlementServiceImpl implements UserEntitlementService {

    @Autowired
    private DailyQuotaService dailyQuotaService;

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MembershipOrderService membershipOrderService;;

    private static final List<String> VALID_PLANS = Arrays.stream(PlanCategory.values())
            .map(PlanCategory::getName)
            .map(String::toUpperCase)
            .toList();

    @Override
    public boolean canUsePlan(Long userId, String plan) {

        // 1. 参数校验
        if (userId == null || plan == null) {
            return false;
        }
        
        String upperPlan = plan.toUpperCase();
        PlanCategory planCategory;
        try {
            planCategory = PlanCategory.fromName(upperPlan);
        } catch (IllegalArgumentException e) {
            return false;
        }

        // 2. 【新增】检查是否有临时体验权益（来自券）
        if (hasTemporaryEntitlement(userId, upperPlan)) {
            return true; // 有券，直接放行！
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

        // 5. 检查权限和额度
        return checkQuotaForPlan(userId, level, planCategory);
    }

    /**
     * 检查是否有临时体验权益（来自券）
     */
    private boolean hasTemporaryEntitlement(Long userId, String plan) {
        String key = "user:entitlement:" + plan.toLowerCase() + ":" + userId;
        String value = redisTemplate.opsForValue().get(key);
        return value != null; // 存在即有效
    }

    /**
     * 检查当日额度
     */
    private boolean checkQuotaForPlan(Long userId, int userLevel, PlanCategory plan) {
        // 检查权限
        if (!plan.hasPermission(userLevel)) {
            return false;
        }

        // 检查额度
        int dailyQuota = plan.getDailyQuota(userLevel);
        int usedToday = dailyQuotaService.getUsedToday(userId, plan.getName());

        return usedToday < dailyQuota;
    }

    /**
     * 扣减额度
     */
    @Override
    public void consumeQuota(Long userId, String plan) {
        if (userId == null || plan == null) {
            log.warn("consumeQuota called with invalid args: userId={}, plan={}", userId, plan);
            return;
        }

        String upperPlan = plan.toUpperCase();
        if (!VALID_PLANS.contains(upperPlan)) {
            log.warn("Invalid plan for consume: {}", plan);
            return;
        }

        //如果是临时权益，直接删除（一次性）
        String tempKey = "user:entitlement:" + upperPlan.toLowerCase() + ":" + userId;
        String tempValue = redisTemplate.opsForValue().get(tempKey);
        if (tempValue != null) {
            redisTemplate.delete(tempKey);
            log.debug("Consumed temporary entitlement: userId={}, plan={}", userId, upperPlan);
            return;
        }

        // 直接递增使用次数（Redis Hash 已保证原子性）
        dailyQuotaService.incrementUsed(userId, upperPlan);

        log.debug("Consumed quota: userId={}, plan={}", userId, upperPlan);
    }

    /**
     * 检查是否是会员
     */
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

    @Override
    public void grantEntitlement(Long userId, CouponTemplate template) {
        String feature = template.getEntitlementFeature();
        if (feature == null) {
            log.warn("权益券 {} 未配置 entitlementFeature", template.getTemplateCode());
            return;
        }

        try {
            CouponTemplateEntitlementFeature entitlementFeature = CouponTemplateEntitlementFeature.fromCode(feature);
            
            if (entitlementFeature.isFeatureEntitlement()) {
                grantFeatureEntitlement(userId, feature, template);
            } else if (entitlementFeature.isMembershipEntitlement()) {
                grantOneDayMembership(userId, template);
            } else {
                log.warn("未知的权益类型: {}", feature);
            }
        } catch (IllegalArgumentException e) {
            log.warn("未知的权益类型: {}", feature);
        }
    }

    /**
     * 发放 HD / PRO 临时功能权益
     */
    private void grantFeatureEntitlement(Long userId, String feature, CouponTemplate template) {
        // 计算过期时间（优先用 fixed_valid_to，否则用 valid_days）
        Duration ttl = calculateTtl(template);
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            ttl = Duration.ofHours(24); // 默认 24 小时
        }

        String key = "user:entitlement:" + feature.toLowerCase() + ":" + userId;
        redisTemplate.opsForValue().set(key, "1", ttl);

        log.info("发放功能体验权益成功: userId={}, feature={}, ttl={}", userId, feature, ttl);
    }


    /**
     * 根据模板计算 TTL
     */
    private Duration calculateTtl(CouponTemplate template) {
        if (template.getFixedValidTo() != null) {
            Duration d = Duration.between(LocalDateTime.now(), template.getFixedValidTo());
            return d.isNegative() ? Duration.ZERO : d;
        }
        if (template.getValidDays() != null && template.getValidDays() > 0) {
            return Duration.ofDays(template.getValidDays());
        }
        return null;
    }


    /**
     * 发放临时会员体验
     */
    private void grantOneDayMembership(Long userId, CouponTemplate template) {
        // 1. 避免重复发放（幂等）
        User user = userService.getById(userId);
        if (user == null) return;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayLater = now.plusDays(1);

        // 如果当前已是会员且未过期，且过期时间 > 1，则不覆盖
        if (user.getMemberExpireAt() != null && user.getMemberExpireAt().isAfter(oneDayLater)) {
            log.info("用户 {} 已有更长期限会员，跳过 MONEDAY 发放", userId);
            return;
        }

        membershipOrderService.activateMembership(userId, MembershipType.MONEDAY.getCode());

        log.info("发放 1 天会员体验成功: userId={}", userId);
    }

    /**
     * 检查会员额度
     */
    private boolean checkMemberQuota(User user, String plan) {
        String upperPlan = plan.toUpperCase();

        PlanCategory category;
        try {
            category = PlanCategory.fromName(upperPlan);
        } catch (IllegalArgumentException e) {
            return false;
        }
        switch (category) {
            case BASIC : return true; // 会员 BASIC 无限
            case HD : return dailyQuotaService.getUsedToday(user.getId(), PlanCategory.HD.getName()) < 100;
                 // 会员 HD 100次/天
            case PRO : return dailyQuotaService.getUsedToday(user.getId(), PlanCategory.PRO.getName()) < 30;
                // 会员 PRO 30次/天
            default:
                return false;
        }
    }
}