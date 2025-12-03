package cn.loblok.upc.controller;

import cn.loblok.upc.annotation.CurrentUser;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.service.UserCouponService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *   用户优惠券控制器
 * </p>
 *
 * @author loblok
 * @since 2025-12-02
 */
@RestController
@RequestMapping("/api/user-coupon")
public class UserCouponController {

    private UserCouponService couponService;

    @PostMapping("/grab-free-ai-voucher")
    public Result<Void> grabFreeAIVoucher(@CurrentUser Long userId) {
        // 业务：抢“AI绘图免费体验券”
        couponService.grabLimitedCoupon(userId, "AI_PAINT_FREE_TRIAL");
        return Result.success();
    }

}
