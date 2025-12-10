package cn.loblok.upc.controller;

import cn.loblok.upc.dto.PayParamsResponse;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.service.MembershipOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *   会员订单控制器
 * </p>
 *
 * @author loblok
 * @since 2025-12-03
 */
@RestController
@RequestMapping("/api/membership-orders")
public class MembershipOrderController {
    
    @Autowired
    private MembershipOrderService membershipOrderService;
    
    /**
     * 创建会员订单
     * @param userId 用户ID
     * @param membershipType 会员类型
     * @return 创建结果
     */
    @PostMapping
    public Result<?> createOrder(@RequestParam Long userId, @RequestParam String membershipType) {
        return membershipOrderService.createOrder(userId, membershipType);
    }

    /**
     * 获取支付参数
     * @param orderNo 订单号
     * @return 支付参数
     */
    @GetMapping("/{orderNo}/pay-params")
    public Result<PayParamsResponse> getPayParams(@PathVariable String orderNo) {
        try {
            PayParamsResponse payParams = membershipOrderService.generatePayParams(orderNo);
            return Result.success(payParams);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}