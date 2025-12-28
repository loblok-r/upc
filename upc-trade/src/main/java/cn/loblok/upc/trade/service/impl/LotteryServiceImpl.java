package cn.loblok.upc.trade.service.impl;

import cn.loblok.upc.api.user.feign.UserFeignClient;
import cn.loblok.upc.api.worker.dto.ProductDeliveryMsgDTO;
import cn.loblok.upc.common.enums.MallOrderStatus;
import cn.loblok.upc.common.enums.UserItemSourceType;
import cn.loblok.upc.trade.dto.lottery.DrawResultDTO;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.trade.dto.lottery.UserDrawInfo;
import cn.loblok.upc.trade.entity.FlashSaleOrders;
import cn.loblok.upc.trade.entity.Products;
import cn.loblok.upc.common.exception.BizException;
import cn.loblok.upc.trade.service.FOrdersService;
import cn.loblok.upc.trade.service.LotteryService;
import cn.loblok.upc.trade.service.ProductsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@Slf4j
@RequiredArgsConstructor
public class LotteryServiceImpl implements LotteryService {


    private final ProductsService productsService;
    private final UserFeignClient userFeignClient;
    private final RabbitTemplate rabbitTemplate;
    private final FOrdersService FOrdersService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<DrawResultDTO> draw(Long userId) {


        Result<Integer> deductResult = userFeignClient.consumeLotteryTicket(userId);

        if (deductResult.getCode() != 200) {
            return Result.error("抽奖机会不足或扣减失败");
        }

        int remainingChances = deductResult.getData();

        // 随机抽取奖品
        Products prize = productsService.drawRandomPrize();


        // 异步发奖
        // 发 MQ 给 Worker 模块去发奖
        deliverPrizeAsync(userId, prize);


        // 构造返回结果
        DrawResultDTO result = new DrawResultDTO();
        result.setPrizeId(prize.getId());

        UserDrawInfo userInfo = new UserDrawInfo();
        userInfo.setId(userId);
        result.setUser(userInfo);

        log.info("用户 {} 抽中奖品: {}, 剩余次数: {}", userId, prize.getId(),remainingChances);

        return Result.success(result);
    }

    /**
     * 异步发奖
     * @param userId
     * @param prize
     */

    private void deliverPrizeAsync(Long userId, Products prize) {

        // 创建订单
        FlashSaleOrders order = new FlashSaleOrders();
        order.setUserId(userId);
        order.setFlashSaleId(null);
        order.setProductId(prize.getId());
        order.setPointsSpent(prize.getPointsRequired());

        //等待发放
        order.setMallOrderStatus(MallOrderStatus.PENDING_EXTERNAL);
        //没有过期时间
        order.setReserveExpiresAt(null);
        order.setCreatedAt(LocalDateTime.now());

        boolean saved = FOrdersService.save(order);
        if (!saved) {
            throw new BizException("抢购失败，请重试");
        }

        ProductDeliveryMsgDTO msg = ProductDeliveryMsgDTO.builder()
                .orderId(Long.parseLong(order.getId()))
                .userId(userId)
                .source(UserItemSourceType.EVENT_REWARD.getDescription())
                .category(prize.getCategory().name())
                .productName(prize.getName())
                .deliveryConfig(prize.getDeliveryConfig())
                .build();

        rabbitTemplate.convertAndSend("upc.direct.exchange", "mq.route.product_delivery",msg);
    }
}