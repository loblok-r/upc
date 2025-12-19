package cn.loblok.upc.modules.payment.strategy;

import cn.loblok.upc.modules.payment.client.AlipayAClient;
import cn.loblok.upc.modules.payment.client.WechatPayClient;
import cn.loblok.upc.modules.payment.dto.PaymentResponse;
import cn.loblok.upc.modules.payment.dto.PaymentStatusResponse;
import cn.loblok.upc.modules.payment.dto.WechatNativePayResponse;
import cn.loblok.upc.modules.payment.dto.WechatUnifiedOrderRequest;
import cn.loblok.upc.modules.member.entity.TOrder;
import cn.loblok.upc.common.enums.PayMethodEnum;
import cn.loblok.upc.modules.member.enums.TOrderStatus;
import cn.loblok.upc.mapper.TOrderMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@AllArgsConstructor
public class PaymentServiceImpl implements PaymentService {


    private final WechatPayClient wechatPayClient;
    

    private final AlipayAClient alipayAClient;


    private final TOrderMapper orderMapper;

    private final WeChatPayService weChatPayService;

    private final AliPayService aliPayService;

    @Override
    public PaymentResponse createOrder(String orderId, BigDecimal price, String paymentMethod, String description) {
        PaymentResponse response = new PaymentResponse();
        response.setOrderId(orderId);

        try {
            // 根据支付方式处理不同逻辑
            if (PayMethodEnum.WECHAT_PAY.getCode().equalsIgnoreCase(paymentMethod)) {
                // 微信 Native 支付
                WechatUnifiedOrderRequest request = WechatUnifiedOrderRequest.builder()
                        .outTradeNo(orderId)
                        .totalFee(price.multiply(new BigDecimal("100")).intValue()) // 转换为分
                        .description(description)
                        .spbillCreateIp("127.0.0.1") // 实际应用中应该从请求中获取真实IP
                        .notifyUrl("https://yourdomain.com/api/payment/notify/wechat") // 微信支付回调地址
                        .tradeType("NATIVE")
                        .build();

                WechatNativePayResponse wechatResponse = wechatPayClient.unifiedOrderNative(request);
                response.setQrCodeUrl(wechatResponse.getCodeUrl());
                response.setSuccess(true);
            } else if (PayMethodEnum.ALI_PAY.getCode().equalsIgnoreCase(paymentMethod)) {
                // 支付宝支付
                PaymentResponse alipayResponse = alipayAClient.createAlipayOrder(orderId, price, description);
                response.setPaymentUrl(alipayResponse.getPaymentUrl());
                response.setSuccess(true);
            } else {
                response.setSuccess(false);
                return response;
            }
        } catch (Exception e) {
            log.error("创建支付订单失败: ", e);
            response.setSuccess(false);
            return response;
        }

        return response;
    }




    /**
     *
     *
     * 获取支付状态
     * 逻辑：查库 -> (如果是PENDING) -> 调第三方接口查 -> 更新库 -> 返回结果
     */
    @Override
    public PaymentStatusResponse getPaymentStatus(String orderId) {

        // 先查本地数据库
        TOrder order = orderMapper.selectById(orderId);
        if(order == null){
            return null;
        }


        // 如果本地已经是最终状态 (SUCCESS / FAILED)，直接返回
        if (TOrderStatus.SUCCESS.getCode().equals(order.getStatus()) || TOrderStatus.FAILED.getCode().equals(order.getStatus())) {
            return buildResponse(order);
        }

        //    如果本地是 PENDING，说明可能：
        //    a. 用户还没付
        //    b. 用户付了，但微信/支付宝的回调还没发过来（或者回调失败了）
        //    为了防止回调丢失导致用户一直转圈，这里进行“主动查询”

        String remoteStatus = queryThirdPartyStatus(order);

        //   如果第三方状态变了，同步更新本地数据库 (相当于手动触发了回调逻辑)
        if (!remoteStatus.equals(order.getStatus())) {
            order.setStatus(remoteStatus);
            if (TOrderStatus.SUCCESS.getCode().equals(remoteStatus)) {
                order.setPaidTime(LocalDateTime.now());
                // TODO: 这里触发发货逻辑 (给用户加会员时长等)
                // userService.grantVip(order.getUserId(), order.getPlanId());
            }
            orderMapper.insert( order); // 落库保存
        }

        return buildResponse(order);
    }

    /**
     * 辅助方法：调用第三方查单接口
     */
    private String queryThirdPartyStatus(TOrder order) {
        try {
            if (PayMethodEnum.WECHAT_PAY.getCode().equals(order.getPaymentMethod())) {
                // 调用微信查单 API
                // 返回值通常映射为我们自己的状态: SUCCESS, PENDING, FAILED
                return weChatPayService.queryOrderStatus(order.getOrderId());
            } else if (PayMethodEnum.ALI_PAY.getCode().equals(order.getPaymentMethod())) {
                // 调用支付宝查单 API
                return aliPayService.queryOrderStatus(order.getOrderId());
            }
        } catch (Exception e) {
            // 如果查单网络超时，不要报错，暂时维持 PENDING，等下一次轮询再试
            log.error("主动查单失败: {}", order.getOrderId(), e);
        }
        return TOrderStatus.PENDING.getCode(); // 默认保持原样
    }

    /**
     * 辅助方法：构建返回值
     */
    private PaymentStatusResponse buildResponse(TOrder order) {
        PaymentStatusResponse response = new PaymentStatusResponse();
        response.setOrderId(order.getOrderId());
        response.setStatus(order.getStatus()); // SUCCESS, PENDING, FAILED
        response.setAmount(order.getAmount());

        if (order.getPaidTime() != null) {
            response.setPaidAt(order.getPaidTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        return response;
    }

    @Override
    public void cancelPayment(String orderId) {
        // 在实际应用中，这里应该调用第三方支付接口取消支付或更新订单状态
        log.info("取消支付订单: {}", orderId);
    }
}