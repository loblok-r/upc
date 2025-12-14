package cn.loblok.upc.controller;

import cn.loblok.upc.annotation.CurrentUser;
import cn.loblok.upc.dto.CreatePaymentRequest;
import cn.loblok.upc.dto.PaymentResponse;
import cn.loblok.upc.dto.PaymentStatusResponse;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.enums.PlanEnum;
import cn.loblok.upc.service.PaymentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/payment")
@Slf4j
@AllArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 创建支付订单
     * @param request 创建订单请求
     * @param userId 当前用户ID
     * @return 创建结果
     */
    @PostMapping("/create")
    public Result<PaymentResponse> createPaymentOrder(
            @RequestBody CreatePaymentRequest request,
            @CurrentUser Long userId) {
        log.info("创建订单: {}", request.getOrderId());
        PlanEnum plan = PlanEnum.getByValue(request.getPlanId());
        if (plan == null) {
            return Result.error("套餐不存在");
        }
        // 校验
        // compareTo 返回 0 表示相等
        if (plan.getPrice().compareTo(request.getAmount()) != 0) {
            // 如果价格不一致，说明前端数据可能被篡改，或者是旧缓存
            // 为了安全，拒绝交易，或者提示用户价格已变动
            return Result.error("订单价格发生变动，请刷新页面后重试");
        }

        // 传给支付接口的一定要是 plan.getPrice()，即后端价格
        PaymentResponse response = paymentService.createOrder(
                request.getOrderId(),
                plan.getPrice(), // 用后端价格
                request.getPaymentMethod(),
                request.getDescription()
        );

        return Result.success(new PaymentResponse());
    }


    /**
     * 查询支付订单状态
     * @param orderId 订单ID
     * @return 订单状态
     */
    @GetMapping("/status/{orderId}")
    public Result<PaymentStatusResponse> getPaymentStatus(@PathVariable String orderId) {
        log.info("查询订单: {}", orderId);

        PaymentStatusResponse response = paymentService.getPaymentStatus(orderId);
        return Result.success(response);
    }

    /**
     * 取消支付订单
     * @param orderId 订单ID
     * @return 是否成功
     */
    @PostMapping("/cancel/{orderId}")
    public Result<Boolean> cancelPayment(@PathVariable String orderId) {
        log.info("取消订单: {}", orderId);

        try {
            paymentService.cancelPayment(orderId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
        return Result.success(true);
    }


}