package cn.loblok.upc.service;

import cn.loblok.upc.entity.UserCoupon;
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
     * 用户领取优惠券（核心方法）
     */
    public void grantCoupon(Long userId, String templateCode);


}
