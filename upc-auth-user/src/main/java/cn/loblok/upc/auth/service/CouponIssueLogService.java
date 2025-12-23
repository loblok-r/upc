package cn.loblok.upc.auth.service;


import cn.loblok.upc.auth.entity.CouponIssueLog;
import cn.loblok.upc.auth.entity.IssueContext;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 优惠券发放记录表 服务类
 * </p>
 *
 * @author loblok
 * @since 2025-12-02
 */
public interface CouponIssueLogService extends IService<CouponIssueLog> {
    // 通用发放接口
    void issueCoupon(Long userId, String templateCode, IssueContext context);
}
