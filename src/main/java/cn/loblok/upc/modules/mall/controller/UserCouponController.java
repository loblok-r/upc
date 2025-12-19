package cn.loblok.upc.modules.mall.controller;

import cn.loblok.upc.common.annotation.CurrentUser;
import cn.loblok.upc.modules.mall.dto.CouponResponse;
import cn.loblok.upc.common.base.PageResult;
import cn.loblok.upc.modules.mall.service.UserCouponService;
import cn.loblok.upc.common.base.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/coupons")
@AllArgsConstructor
public class UserCouponController {

    private final UserCouponService couponService;


    @GetMapping("/list")
     public Result<IPage<CouponResponse>>  getUserCoupons(
             @CurrentUser Long userId,
             Integer page,
             Integer pageSize
     ) {
        IPage<CouponResponse> pageResult = couponService.getUserCoupons(userId, page, pageSize);

        PageResult<CouponResponse> response = PageResult.of(pageResult);
        return Result.success(response);
    }


//    @PostMapping("/grab-free-ai-voucher")
//    public Result<Void> grabFreeAIVoucher(@CurrentUser Long userId) {
//        // 业务：抢“AI绘图免费体验券”
//        couponService.grabLimitedCoupon(userId, "AI_PAINT_FREE_TRIAL");
//        return Result.success(null);
//    }

}
