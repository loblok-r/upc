package cn.loblok.upc.auth.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.loblok.upc.auth.entity.CouponIssueLog;
import cn.loblok.upc.auth.entity.IssueContext;
import cn.loblok.upc.auth.enums.IssueStatus;
import cn.loblok.upc.auth.mapper.CouponIssueLogMapper;
import cn.loblok.upc.auth.service.CouponIssueLogService;
import cn.loblok.upc.auth.service.UserCouponService;
import cn.loblok.upc.common.exception.BizException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * <p>
 *      优惠券发放记录表 服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-02
 */
@Service
@Primary
@Slf4j
public class CouponIssueLogServiceImpl extends ServiceImpl<CouponIssueLogMapper, CouponIssueLog> implements CouponIssueLogService {

    @Autowired
    private UserCouponService userCouponService;

    @Autowired
    private CouponIssueLogMapper issueLogMapper;

    @Override
    public void issueCoupon(Long userId, String templateCode, IssueContext context) {
        // 1. 幂等检查（防止重复发放）
        if (isAlreadyIssued(userId, templateCode, context.getBizId())) {
            log.info("Coupon already issued, skip. user={}, template={}, bizId={}",
                    userId, templateCode, context.getBizId());
            return;
        }

        try {
            // 2. 调用你已有的安全发放逻辑
            userCouponService.grantCoupon(userId, templateCode);

            // 3. 记录成功日志
            recordSuccess(userId, templateCode, context);

            // 4.  todo 异步推送通知（可选）

        } catch (BizException e) {
            log.warn("Coupon issue failed: user={}, template={}, reason={}",
                    userId, templateCode, e.getMessage());
            recordFailure(userId, templateCode, context, e.getMessage());
            throw e;
        }catch (Exception e) {
            log.error("系统异常，发券失败: user={}, template={}", userId, templateCode, e);
            recordFailure(userId, templateCode, context,"系统异常: " + e.getMessage());
            throw new BizException("发券服务暂时不可用");
        }
    }

    private void recordFailure(Long userId, String templateCode, IssueContext context, String errorMsg) {
        CouponIssueLog log = new CouponIssueLog();
        log.setUserId(userId);
        log.setTemplateCode(templateCode);
        log.setBizType(context.getBizType().name());
        log.setBizId(context.getBizId());
        log.setStatus(IssueStatus.FAILED.getCode());
        log.setErrorMsg(StrUtil.maxLength(errorMsg, 500));

        issueLogMapper.insert(log);
    }

    /**
     * 检查是否已经发放过优惠券
     * @param userId 用户ID
     * @param templateCode 模板编码
     * @param bizId 业务ID
     * @return 是否已发放
     */
    private boolean isAlreadyIssued(Long userId, String templateCode, String bizId) {
        return issueLogMapper.existsByBizId(bizId) != null;
    }

    private void recordSuccess(Long userId, String templateCode, IssueContext ctx) {
        CouponIssueLog log = new CouponIssueLog();
        log.setUserId(userId);
        log.setTemplateCode(templateCode);
        log.setBizType(ctx.getBizType().name());
        log.setBizId(ctx.getBizId());
        log.setStatus(IssueStatus.SUCCESS.getCode());
        issueLogMapper.insert(log);
    }

    // todo pushNotification ...
}
