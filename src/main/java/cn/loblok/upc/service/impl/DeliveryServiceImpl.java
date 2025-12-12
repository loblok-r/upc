package cn.loblok.upc.service.impl;

import cn.loblok.upc.entity.Orders;
import cn.loblok.upc.entity.Products;
import cn.loblok.upc.enums.MallOrderStatus;
import cn.loblok.upc.enums.UserItemSourceType;
import cn.loblok.upc.enums.UserItemType;
import cn.loblok.upc.mapper.FlashOrdersMapper;
import cn.loblok.upc.service.DeliveryService;
import cn.loblok.upc.service.UserItemsService;
import cn.loblok.upc.service.UserService;
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
public class DeliveryServiceImpl implements DeliveryService {


    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final UserService userService;

    private final FlashOrdersMapper flashOrdersMapper;

    private final UserItemsService userItemService;

    @Override
    public void deliverPhysical(Orders order) {
        log.info("开始发货：订单ID={}", order.getId());
//         只需确保订单状态为 "待发货"
        order.setMallOrderStatus(MallOrderStatus.AWAITING_SHIPMENT);
        flashOrdersMapper.updateById(order);
//         后续由运营/WMS系统处理发货
    }

    @Override
    public void deliverVirtual(Orders order, Products product) {
        log.info("开始发放虚拟资产：订单ID={}", order.getId());
        Long userId = order.getUserId();
        Map<String, Object> config = parseConfig(product.getDeliveryConfig());

        String type = (String) config.get("type");

        //发放算力值
        if (UserItemType.COMPUTE_POWER.getValue().equals(type)) {
            Integer amount = (Integer) config.get("amount");
            userService.addComputePower(userId, amount);
        }
        //发放会员
        else if (UserItemType.VIP.getValue().equals(type)) {
            Integer days = (Integer) config.get("duration_days");
            userService.extendVipDays(userId, days);
        }

        order.setMallOrderStatus(MallOrderStatus.COMPLETED);
        flashOrdersMapper.updateById(order);
    }

    @Override
    public void deliverVoucher(Orders order, Products product) {
        log.info("开始发放代金券：订单ID={}", order.getId());
        Long userId = order.getUserId();
        Map<String, Object> config = parseConfig(product.getDeliveryConfig());

        String type = (String) config.get("type");

        String sourceId = order.getId().toString();

        //发放补签卡
        if (UserItemType.RESIGN_CARD.getValue().equals(type)) {
            Integer count = (Integer) config.getOrDefault("count", 1);
            userItemService.addItem(userId,UserItemType.RESIGN_CARD, UserItemSourceType.FLASH_SALE,sourceId,null,count);
        }
        //发放抽奖次数
        else if (UserItemType.LOTTERY_TICKET.getValue().equals(type)) {
            Integer count = (Integer) config.get("count");
            userItemService.addItem(userId, UserItemType.LOTTERY_TICKET, UserItemSourceType.FLASH_SALE, sourceId,null,count);
        }

        //发放年度会员折扣兑换码
        else if (UserItemType.ANNUAL_MEMBERSHIP_VOUCHER.getValue().equals(type)) {
            BigDecimal discount = (BigDecimal) config.get("discount_rate");

            Map<String, Object> extra = new HashMap<>();

            extra.put("discount_rate", discount);

            userItemService.addItem(userId, UserItemType.ANNUAL_MEMBERSHIP_VOUCHER, UserItemSourceType.FLASH_SALE, sourceId,extra,1);
        }
        //发放优惠券
        else if ("discount_coupon".equals(type)) {
            String scope = (String)config.get("scope");
            BigDecimal discount = (BigDecimal) config.get("discount_rate");
            //todo 发放优惠券
//           couponService.issueDiscountCoupon(userId, discount, scope, sourceId);
        }

        order.setMallOrderStatus(MallOrderStatus.COMPLETED);
        flashOrdersMapper.updateById(order);
    }



    @Override
    public void deliverThirdPartyVirtual(Orders order, Products product) {
        // 标记为“外部发放中”
        order.setMallOrderStatus(MallOrderStatus.PENDING_EXTERNAL);
        flashOrdersMapper.updateById(order);
        // todo 发送到异步队列（如 RocketMQ）
        //externalDeliveryProducer.send(order.getId());
    }


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