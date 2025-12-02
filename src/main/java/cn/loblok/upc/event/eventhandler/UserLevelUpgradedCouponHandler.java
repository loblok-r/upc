package cn.loblok.upc.event.eventhandler;

import cn.loblok.upc.entity.IssueContext;
import cn.loblok.upc.enums.BizType;
import cn.loblok.upc.enums.CouponTemplateType;
import cn.loblok.upc.event.UserLevelUpgradedEvent;
import cn.loblok.upc.service.CouponIssueLogService;
import cn.loblok.upc.util.PrefixUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 用户等级升级事件处理器
 */
@Component
@Slf4j
public class UserLevelUpgradedCouponHandler {
    @Autowired
    private CouponIssueLogService couponIssueService;

    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleUserLevelUpgraded(UserLevelUpgradedEvent event) {
        Long userId = event.getUserId();
        int oldLevel = event.getOldLevel();
        int newLevel = event.getNewLevel();

        // 根据升级目标等级，发放不同优惠券
        if (newLevel == 3) {
            issueLevel3EliteCoupon(userId, event);
        } else if (newLevel == 5) {
            issueLevel5EliteCoupon(userId, event);
        }
        // 其他等级...
    }

    private void issueLevel3EliteCoupon(Long userId, UserLevelUpgradedEvent event) {
        IssueContext context = IssueContext.builder()
                .bizType(BizType.UPGRADE_PACKAGE)
                .bizId(PrefixUtil.buildLevelUpgradeCouponKey(userId, event.getNewLevel()))
                .build();

        couponIssueService.issueCoupon(
                userId,
                CouponTemplateType.LEVEL_3_ELITE.getTemplateCode(),
                context
        );
    }
    private void issueLevel5EliteCoupon(Long userId, UserLevelUpgradedEvent event) {
        IssueContext context = IssueContext.builder()
                .bizType(BizType.UPGRADE_PACKAGE)
                .bizId(PrefixUtil.buildLevelUpgradeCouponKey(userId, event.getNewLevel()))
                .build();

        couponIssueService.issueCoupon(
                userId,
                CouponTemplateType.LEVEL_5_ELITE.getTemplateCode(),
                context
        );
    }

}