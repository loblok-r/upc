package cn.loblok.upc.controller;

import cn.loblok.upc.annotation.CurrentUser;
import cn.loblok.upc.dto.FlashOrderRequestDTO;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.entity.FlashOrders;
import cn.loblok.upc.entity.FlashSales;
import cn.loblok.upc.entity.Products;
import cn.loblok.upc.enums.MallOrderStatus;
import cn.loblok.upc.exception.BizException;
import cn.loblok.upc.service.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

/**
 * <p>
 *     秒杀订单
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
@RestController
@RequestMapping("/api/mall")
@AllArgsConstructor
@Slf4j
public class FlashOrdersController {
    

    private final FlashOrdersService flashOrdersService;
    

    private final FlashSalesService flashSalesService;


    private final PointsService pointsService;

    private final DeliveryService deliveryService;

    private final ProductsService productsService;

    /**
     * 秒杀抢购接口
     *
     * @param userId 当前用户ID
     * @param request 请求参数
     * @return 抢购结果
     */
    @PostMapping("/flash/grab")
    public Result<String> purchaseFlashSale(@CurrentUser Long userId, 
                                          @Valid @RequestBody FlashOrderRequestDTO request) {

        log.info("用户{}正在抢购秒杀活动{}", userId, request.getFlashSaleId());
        // 检查秒杀活动是否存在且有效
        FlashSales flashSale = flashSalesService.getById(request.getFlashSaleId());

        if (flashSale == null) {
            throw new BizException("秒杀活动不存在");
        }
        
        // 检查活动状态
        if (!"active".equals(flashSale.getStatus())) {
            throw new BizException("秒杀活动未开始或已结束");
        }

        // 检查时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(flashSale.getStartTime()) || now.isAfter(flashSale.getEndTime())) {
            throw new BizException("秒杀活动未开始或已结束");
        }
        
        // 检查库存
        if (flashSale.getRemainingStock() <= 0) {
            throw new BizException("秒杀商品已售完");
        }

        // TODO: 这里应该使用乐观锁更新库存
        // 减少库存
        flashSale.setRemainingStock(flashSale.getRemainingStock() - 1);
        flashSalesService.updateById(flashSale);

        //扣除积分
        pointsService.reduceUserPoints(userId, flashSale.getSalePrice());


        // TODO: 这里应该加入分布式锁来确保并发安全
        // 创建订单
        FlashOrders order = new FlashOrders();
        order.setUserId(userId);
        order.setFlashSaleId(request.getFlashSaleId());
        order.setProductId(flashSale.getProductId());
        order.setPointsSpent(flashSale.getSalePrice());
        //等待发放
        order.setMallOrderStatus(MallOrderStatus.PENDING_EXTERNAL);
        //没有过期时间
        order.setReserveExpiresAt(null);
        order.setCreatedAt(LocalDateTime.now());
        
        boolean saved = flashOrdersService.save(order);
        if (!saved) {
            throw new BizException("抢购失败，请重试");
        }

        //todo 判断是否是实体商品 发放动作应在订单创建后异步完成，需保证最终一致性。
        // 虚拟商品（算力值，会员，优惠券）发放，事务结束后立即发放

        String productId = flashSale.getProductId();
        Products product = productsService.getById(productId);

        //发放商品
        onFlashGrabSuccess(order, product);

        return Result.success("抢购成功");
    }

    /**
     * 秒杀抢购成功后的处理逻辑
     *
     * @param order 订单信息
     * @param product 商品信息
     */
    public void onFlashGrabSuccess(FlashOrders order, Products product) {


        log.info("用户{}抢购成功，商品{},进入处理逻辑", order.getUserId(), product.getName());

        // 特殊处理：第三方会员（即使类型是 VIRTUAL）
        if (isThirdPartyProduct(product)) {
            deliveryService.deliverThirdPartyVirtual(order, product);
            return;
        }

        // 普通分发
        switch (product.getCategory()) {
            case PHYSICAL:
                deliveryService.deliverPhysical(order);
                break;
            case VIRTUAL:
                deliveryService.deliverVirtual(order, product);
                break;
            case VOUCHER:
                deliveryService.deliverVoucher(order, product);
                break;
            default:
                throw new IllegalStateException("未知商品类型: " + product.getCategory());
        }
    }

    //todo 暂未实现
    private boolean isThirdPartyProduct(Products product) {

        return false;
    }
}