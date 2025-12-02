package cn.loblok.upc.event.eventhandler;

import cn.loblok.upc.entity.IssueContext;
import cn.loblok.upc.enums.BizType;
import cn.loblok.upc.enums.CouponTemplateType;
import cn.loblok.upc.event.UserRegisteredEvent;
import cn.loblok.upc.service.CouponIssueLogService;
import cn.loblok.upc.util.PrefixUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 监听用户注册事件，发放欢迎券
 */
@Component
@Slf4j
public class WelcomeCouponHandler {

    @Autowired
    private CouponIssueLogService couponIssueService;

    @EventListener
    @Async
    public void handleUserRegistered(UserRegisteredEvent event) {
        try {
            IssueContext context = IssueContext.builder()
                    .bizType(BizType.REGISTER_GIFT)
                    .bizId(PrefixUtil.buildRegisterCouponIssueKey(event.getUserId()))
                    .build();

            couponIssueService.issueCoupon(
                    event.getUserId(),
                    CouponTemplateType.WELCOME_2025.getTemplateCode(),
                    context
            );
        } catch (Exception e) {
            log.error("发券失败，投递到重试队列 user={}", event.getUserId(), e);

            // 方案1：发送到延迟队列重试（推荐） todo 集成RocketMQ
//            retryQueueProducer.sendRetryMessage(
//                    "WELCOME_COUPON_RETRY",
//                    event.getUserId(),
//                    5 /* 5分钟后重试 */
//            );

            // 方案2：记录到 DB，由定时任务扫描补偿
            // failedTaskService.save(new FailedTask(...));
        }
    }

    // todo 监听邀请成功事件
//    @EventListener
//    public void onInviteSuccess(InviteSuccessEvent event) {
//        Long inviterId = event.getInviterId();
//
//        // 检查是否已发过（防重复）
//        if (rewardLogService.exists(inviterId, "INVITE_REWARD")) {
//            return;
//        }
//
//        // 检查邀请人数是否达标
//        long inviteCount = inviteService.countValidInvites(inviterId);
//        if (inviteCount >= 3) {
//            IssueContext ctx = IssueContext.builder()
//                    .bizType("INVITE_REWARD")
//                    .bizId("invite_" + inviterId)
//                    .build();
//            couponIssueService.issueCoupon(inviterId, "INVITE_10YUAN", ctx);
//            rewardLogService.record(inviterId, "INVITE_REWARD");
//        }
//    }
}