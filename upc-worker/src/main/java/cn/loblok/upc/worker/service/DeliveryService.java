package cn.loblok.upc.worker.service;

import cn.loblok.upc.api.trade.feign.TradeFeignClient;
import cn.loblok.upc.api.user.feign.UserFeignClient;
import cn.loblok.upc.api.worker.dto.ProductDeliveryMsgDTO;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.enums.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class DeliveryService {


    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final TradeFeignClient tradeFeignClient;

    private final UserFeignClient userFeignClient;


    public void delivery(ProductDeliveryMsgDTO msg) {
        log.info("用户{}下订单成功，商品{},进入发放逻辑", msg.getUserId(), msg.getProductName());

        // 普通分发
        switch (ProductType.valueOf(msg.getCategory())) {
            case PHYSICAL: //VIRTUAL, VOUCHER, PHYSICAL
                this.deliverPhysical(msg);
                break;
            case VIRTUAL:
                this.deliverVirtual(msg);
                break;
            case VOUCHER:
                this.deliverVoucher(msg);
                break;
            default:
                throw new IllegalStateException("未知商品类型: " + msg.getCategory());
        }
    }



    public void deliverVirtual(ProductDeliveryMsgDTO msg) {

        String orderId = String.valueOf(msg.getOrderId());
        log.info("开始发放虚拟资产：订单ID={}", orderId);
        Long userId = msg.getUserId();
        Map<String, Object> config = parseConfig(msg.getDeliveryConfig());

        String type = (String) config.get("type");

        //发放算力值
        if (UserItemType.COMPUTE_POWER.getValue().equals(type)) {
            Integer amount = (Integer) config.get("amount");
            Result<Void> result = userFeignClient.addComputingPower(userId, amount);
            if(result.getCode() != 200){
                throw new RuntimeException(result.getMsg());
            }
        }
        //发放会员
        else if (UserItemType.VIP.getValue().equals(type)) {
            Integer days = (Integer) config.get("duration_days");
            Result<Void> result = userFeignClient.extendVipDays(userId, days);
            if(result.getCode() != 200){
                throw new RuntimeException(result.getMsg());
            }
        }

        if(orderId != null){
            // 发放成功，通知 Trade 模块将订单设为 COMPLETED (已完成)
            String orderStatus = UserItemType.VIP.getValue().equals(type) 
                ? MembershipOrderStatus.COMPLETED.getValue() 
                : MallOrderStatus.COMPLETED.getStatus();
            tradeFeignClient.updateOrderStatus(orderId, orderStatus);
        } else {
            log.info("订单ID={}为空，不更新订单状态", orderId);
        }

    }


    public void deliverVoucher(ProductDeliveryMsgDTO msg) {

        String orderId = String.valueOf(msg.getOrderId());
        log.info("开始发放代金券：订单ID={}", msg.getOrderId());
        Long userId = msg.getUserId();
        Map<String, Object> config = parseConfig(msg.getDeliveryConfig());

        String type = (String) config.get("type");

        String sourceId = String.valueOf(msg.getOrderId());

        //发放补签卡
        if (UserItemType.RESIGN_CARD.getValue().equals(type)) {
            Integer count = (Integer) config.getOrDefault("count", 1);
            Result<Void> result = userFeignClient.addUserItem(userId, UserItemType.RESIGN_CARD,
                    UserItemSourceType.valueOf(msg.getSource()), sourceId, null, count);
            if(result.getCode() != 200){
                throw new RuntimeException(result.getMsg());
            }
        }
        //发放抽奖次数
        else if (UserItemType.LOTTERY_TICKET.getValue().equals(type)) {
            Integer count = (Integer) config.get("count");
            Result<Void> result = userFeignClient.addUserItem(userId, UserItemType.LOTTERY_TICKET,
                    UserItemSourceType.valueOf(msg.getSource()), sourceId, null, count);
            if(result.getCode() != 200){
                throw new RuntimeException(result.getMsg());
            }
        }

        //发放年度会员折扣兑换码
        else if (UserItemType.ANNUAL_MEMBERSHIP_VOUCHER.getValue().equals(type)) {
            BigDecimal discount = (BigDecimal) config.get("discount_rate");

            Map<String, Object> extra = new HashMap<>();

            extra.put("discount_rate", discount);

            Result<Void> result = userFeignClient.addUserItem(userId, UserItemType.ANNUAL_MEMBERSHIP_VOUCHER,
                    UserItemSourceType.valueOf(msg.getSource()), sourceId, extra, 1);
            if(result.getCode() != 200){
                throw new RuntimeException(result.getMsg());
            }
        }
        //发放优惠券
        else if (UserItemType.DISCOUNT_COUPON.getValue().equals(type)) {
            String scope = (String)config.get("scope");
            BigDecimal discount = (BigDecimal) config.get("discount_rate");
            //todo 发放优惠券
//           couponService.issueDiscountCoupon(userId, discount, scope, sourceId);
        }

        if(orderId != null){
            // 2. 发放成功，通知 Trade 模块将订单设为 COMPLETED (已完成)
            tradeFeignClient.updateOrderStatus(orderId, MallOrderStatus.COMPLETED.getStatus());
        }else{
            log.info("订单ID={}为空，不更新订单状态", orderId);
        }
    }


    /**
     * 发放实物资产
     */
    public void deliverPhysical(ProductDeliveryMsgDTO msg) {
//         只需确保订单状态为 "待发货"
//         后续由运营/WMS系统处理发货
    }

//    /**
//     * 发放第三方虚拟资产
//     */
//    public void deliverThirdPartyVirtual(ProductDeliveryMsgDTO msg) {
//        // 标记为“外部发放中”
//        tradeFeignClient.updateOrderStatus(msg.getOrderId(), "PENDING_EXTERNAL");
//    }


    private Map<String, Object> parseConfig(String deliveryConfig) {


        if (deliveryConfig == null || deliveryConfig.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(deliveryConfig, Map.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse deliveryConfig: " + deliveryConfig, e);
        }
    }


}