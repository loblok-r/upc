package cn.loblok.upc.service;

import cn.loblok.upc.dto.CouponResponse;
import cn.loblok.upc.entity.IssueContext;
import cn.loblok.upc.entity.UserCoupon;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *   管理用户优惠券实例（核心业务） 服务类
 * </p>
 *
 * @author loblok
 * @since 2025-12-02
 */
public interface UserCouponService extends IService<UserCoupon> {

    /**
     * 系统发放优惠券（核心方法）
     */
    public void grantCoupon(Long userId, String templateCode);

    /**
     * 用户领取限时优惠券（核心方法）
     */
    void grabLimitedCoupon(Long userId, String activityCode);

    /**
     * 发送欢迎券（核心方法）
     */
    void grantWelcomeCoupons(Long userId, IssueContext context);

    /**
     * 用户使用优惠券（核心方法）
     */
    boolean useCoupon(Long userId, Long couponId);


    IPage<CouponResponse> getUserCoupons(Long userId, Integer pageNum, Integer pageSize);
}
