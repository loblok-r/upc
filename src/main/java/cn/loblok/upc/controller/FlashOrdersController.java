package cn.loblok.upc.controller;

import cn.loblok.upc.annotation.CurrentUser;
import cn.loblok.upc.dto.FlashOrderRequestDTO;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.entity.FlashOrders;
import cn.loblok.upc.entity.FlashSales;
import cn.loblok.upc.exception.BizException;
import cn.loblok.upc.service.FlashOrdersService;
import cn.loblok.upc.service.FlashSalesService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/flash")
@Slf4j
public class FlashOrdersController {
    
    @Autowired
    private FlashOrdersService flashOrdersService;
    
    @Autowired
    private FlashSalesService flashSalesService;

    /**
     * 秒杀抢购接口
     *
     * @param userId 当前用户ID
     * @param request 请求参数
     * @return 抢购结果
     */
    @PostMapping("/grab")
    public Result<String> purchaseFlashSale(@CurrentUser Long userId, 
                                          @Valid @RequestBody FlashOrderRequestDTO request) {

        log.info("用户{}正在抢购秒杀活动{}", userId, request.getFlashSaleId());
        // 检查秒杀活动是否存在且有效
        FlashSales flashSale = flashSalesService.getById(request.getFlashSaleId());
        if (flashSale == null) {
            throw new BizException("秒杀活动不存在");
        }
        
        // 检查活动状态
        if (!"ACTIVE".equals(flashSale.getStatus())) {
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
        
        // TODO: 这里应该加入分布式锁来确保并发安全
        // 创建订单
        FlashOrders order = new FlashOrders();
        order.setUserId(userId);
        order.setFlashSaleId(request.getFlashSaleId());
        order.setProductId(flashSale.getProductId());
        order.setPointsSpent(flashSale.getSalePrice());
        order.setOrderStatus("RESERVED"); // 预定状态
        order.setReserveExpiresAt(LocalDateTime.now().plusMinutes(10)); // 预定保留10分钟
        order.setCreatedAt(LocalDateTime.now());
        
        boolean saved = flashOrdersService.save(order);
        if (!saved) {
            throw new BizException("抢购失败，请重试");
        }
        
        // TODO: 这里应该使用乐观锁更新库存
        // 减少库存
        
        return Result.success("抢购成功，请尽快支付");
    }
}