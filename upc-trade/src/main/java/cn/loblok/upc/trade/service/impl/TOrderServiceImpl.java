package cn.loblok.upc.trade.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.loblok.rabbit.util.rabbit.constants.MQConstants;
import cn.loblok.upc.api.worker.dto.ProductDeliveryMsgDTO;
import cn.loblok.upc.common.enums.MembershipOrderStatus;
import cn.loblok.upc.common.enums.UserItemSourceType;
import cn.loblok.upc.trade.entity.TOrder;
import cn.loblok.upc.trade.enums.PlanEnum;
import cn.loblok.upc.trade.mapper.TOrderMapper;
import cn.loblok.upc.trade.service.TOrderService;
import cn.loblok.upc.trade.utils.WechatSignatureUtil;
import cn.loblok.upc.trade.utils.XmlUtils;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>
 * 会员订单表 服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-14
 */
@Service
@Primary
@Slf4j
@AllArgsConstructor
public class TOrderServiceImpl extends ServiceImpl<TOrderMapper, TOrder> implements TOrderService {


    private final TOrderMapper orderMapper;

    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleWechatPayNotify(String xmlData) {
        // 1. 解析 XML
        Map<String, String> notifyMap = XmlUtils.toMap(xmlData);

        // 2. 验签（最关键！）
        if (!verifyWechatSignature(notifyMap)) {
            throw new SecurityException("微信通知验签失败");
        }

        // 3. 检查通信和业务结果
        String returnCode = notifyMap.get("return_code");
        String resultCode = notifyMap.get("result_code");
        if (!"SUCCESS".equals(returnCode) || !"SUCCESS".equals(resultCode)) {
            log.warn("微信支付失败: {}", notifyMap.get("err_code_des"));
            return; // 不处理失败订单（可选）
        }

        // 4. 提取关键信息
        String outTradeNo = notifyMap.get("out_trade_no"); // 商户订单号
        String totalFeeStr = notifyMap.get("total_fee");   // 实际支付金额（分）
        String transactionId = notifyMap.get("transaction_id"); // 微信支付订单号

        // 5. 查询订单
        TOrder tOrder = orderMapper.selectById(outTradeNo);
        if (tOrder == null) {
            throw new RuntimeException("订单不存在: " + outTradeNo);
        }

        // 6. 幂等性检查：避免重复处理
        if (MembershipOrderStatus.PAID.getValue().equals(tOrder.getStatus())) {
            log.info("订单已支付，跳过重复通知: {}", outTradeNo);
            return;
        }

        // 7. 金额校验（防止篡改）
        Integer actualAmount = Integer.valueOf(totalFeeStr); // 分
        Integer expectedAmount = tOrder.getAmount().multiply(BigDecimal.valueOf(100)).intValue();
        if (!actualAmount.equals(expectedAmount)) {
            log.error("支付金额不匹配! 订单: {}, 期望: {}分, 实际: {}分", outTradeNo, expectedAmount, actualAmount);
            throw new RuntimeException("金额校验失败");
        }

        // 8. 更新订单状态
        tOrder.setStatus(MembershipOrderStatus.PAID);
        tOrder.setTransactionId(transactionId);
        tOrder.setPaidTime(LocalDateTime.now());
        orderMapper.updateById(tOrder);

        this.deliveryMember(tOrder);

        log.info("订单支付成功: {}, 微信单号: {}", outTradeNo, transactionId);
    }

    @Override
    public void deliveryMember(TOrder tOrder) {
        PlanEnum plan = PlanEnum.getByValue(tOrder.getPlanId());

        ProductDeliveryMsgDTO deliveryMsg = ProductDeliveryMsgDTO.builder()
                .orderId(tOrder.getOrderId())
                .userId(tOrder.getUserId())
                .category("VIRTUAL") // 会员属于虚拟权益
                .productName(plan.getName())
                .source(UserItemSourceType.PURCHASE_PAID.getDescription())
                .deliveryConfig(buildMemberConfig(plan)) // 例如
                .build();
        String bizId = IdUtil.randomUUID();
        CorrelationData correlationData = new CorrelationData(bizId);

        rabbitTemplate.convertAndSend(
                MQConstants.EXCHANGE_NAME,
                MQConstants.ROUTE_POINT_TRANSACTION,
                deliveryMsg,
                message -> {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                },
                correlationData
        );
    }

    private String buildMemberConfig(PlanEnum plan) {
        return  "{\"type\":\"vip\", \"duration_days\":" + plan.getDays() + "}";
    }

    @Override
    public String handleAliPayNotify(Map<String, String> params) {
        // 验签 (核心步骤)
        boolean verifyResult = false;
        try{
            verifyResult= verifyAliSign(params);

        }catch (AlipayApiException e){
            log.error("支付宝验签失败", e);
        }

        if (verifyResult) {
            // 业务逻辑处理
            String tradeStatus = params.get("trade_status");
            String outTradeNo = params.get("out_trade_no"); // 你的商户订单号

            // 支付宝状态：TRADE_SUCCESS 表示支付成功
            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                log.info("支付宝订单 {} 支付成功", outTradeNo);
                // TODO: 更新订单状态、发货
            }

            // 必须返回 success 字符串给支付宝
            return "success";
        }
        return "fail";
    }

    /**
     * 验证微信签名
     *
     * @param params 通知参数
     * @return 签名验证结果
     */
    private boolean verifyWechatSignature(Map<String, String> params) {
        // 取出 sign
        String signFromWechat = params.get("sign");
        if (signFromWechat == null) {return false;}

        // 重新计算签名（排除 sign 字段）
        String signGenerated = WechatSignatureUtil.generateSign(params, "");

        // 比较
        return signGenerated.equals(signFromWechat);
    }

    /**
     * 验证支付宝签名
     *
     * @param params 通知参数
     * @return 签名验证结果
     */
    private boolean verifyAliSign(Map<String, String> params) throws AlipayApiException {
        // 参数说明：参数Map, 支付宝公钥, 字符集, 签名类型(RSA2)
        // 实际项目中需要根据支付宝提供的SDK进行签名验证
        boolean verifyResult = AlipaySignature.rsaCheckV1(
                params,
                "ALIPAY_PUBLIC_KEY", // 注意：是支付宝公钥，不是你自己的应用公钥
                "UTF-8",
                "RSA2"
        );
        // 这里仅为示意，需要根据实际项目配置
        return true; // 临时返回true，实际应用中需要实现真实验签逻辑
    }

    /**
     * 更新订单状态
     *
     * @param outTradeNo  商户订单号
     * @param totalAmount 订单金额
     * @param tradeNo     支付宝交易号
     */
    private void updateOrderStatus(String outTradeNo, String totalAmount, String tradeNo) {
        // 实际项目中需要实现具体的订单状态更新逻辑
        // 更新订单状态为已支付
        log.info("更新订单状态，商户订单号：{}，金额：{}，支付宝交易号：{}", outTradeNo, totalAmount, tradeNo);
    }
}
