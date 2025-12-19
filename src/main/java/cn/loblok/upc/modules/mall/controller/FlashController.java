package cn.loblok.upc.modules.mall.controller;

import cn.loblok.upc.common.annotation.CurrentUser;
import cn.loblok.upc.modules.mall.dto.FlashOrderRequestDTO;
import cn.loblok.upc.modules.mall.dto.FlashSaleDTO;
import cn.loblok.upc.common.base.PageResult;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.modules.mall.entity.FlashSaleOrders;
import cn.loblok.upc.modules.mall.entity.FlashSales;
import cn.loblok.upc.modules.mall.entity.Products;
import cn.loblok.upc.modules.mall.enums.MallOrderStatus;
import cn.loblok.upc.common.exception.BizException;
import cn.loblok.upc.modules.mall.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
@RequestMapping("/api/mall/flash")
@AllArgsConstructor
@Slf4j
public class FlashController {
    

    private final OrdersService ordersService;
    

    private final FlashSalesService flashSalesService;


    private final PointsService pointsService;

    private final DeliveryService deliveryService;

    private final ProductsService productsService;

    /**
     * 获取秒杀活动列表
     *
     * @param page 页码
     * @param size 页面大小
     * @return 秒杀活动列表
     */
    @GetMapping("/list")
    public Result<PageResult<FlashSaleDTO>> getFlashSales(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String date) {  // 添加日期参数

        log.info("开始获取秒杀活动列表: page={}, size={}, date={}", page, size, date);

        IPage<FlashSales> flashSalePage = new Page<>(page, size);
        QueryWrapper<FlashSales> queryWrapper = new QueryWrapper<>();

        if (date != null && !date.isEmpty()) {
            // 如果有日期参数，只查询该日期的数据
            queryWrapper.apply("DATE(start_time) = {0}", date);
        } else {
            // 默认查询今天、明天、后天
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);
            LocalDate dayAfterTomorrow = today.plusDays(2);

            queryWrapper.and(wrapper -> wrapper
                    .apply("DATE(start_time) = {0}", today)
                    .or()
                    .apply("DATE(start_time) = {0}", tomorrow)
                    .or()
                    .apply("DATE(start_time) = {0}", dayAfterTomorrow)
            );
        }

        // 按状态和时间排序
        queryWrapper.orderByAsc("status", "start_time");

        IPage<FlashSales> result = flashSalesService.page(flashSalePage, queryWrapper);

        // 转换为 DTO
        IPage<FlashSaleDTO> dtoPage = result.convert(flashSale -> {
            FlashSaleDTO dto = new FlashSaleDTO();
            BeanUtils.copyProperties(flashSale, dto);

            // 获取商品名称
            Products product = productsService.getById(flashSale.getProductId());
            if (product != null) {
                dto.setProductName(product.getName());
            }

            return dto;
        });

        return Result.success(PageResult.of(dtoPage));
    }

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
        FlashSaleOrders order = new FlashSaleOrders();
        order.setUserId(userId);
        order.setFlashSaleId(request.getFlashSaleId());
        order.setProductId(flashSale.getProductId());
        order.setPointsSpent(flashSale.getSalePrice());
        //等待发放
        order.setMallOrderStatus(MallOrderStatus.PENDING_EXTERNAL);
        //没有过期时间
        order.setReserveExpiresAt(null);
        order.setCreatedAt(LocalDateTime.now());
        
        boolean saved = ordersService.save(order);
        if (!saved) {
            throw new BizException("抢购失败，请重试");
        }

        //todo 判断是否是实体商品 发放动作应在订单创建后异步完成，需保证最终一致性。
        // 虚拟商品（算力值，会员，优惠券）发放，事务结束后立即发放

        String productId = flashSale.getProductId();
        Products product = productsService.getById(productId);

        //发放商品
        productsService.onOrderSuccess(order, product);

        return Result.success("抢购成功");
    }


}