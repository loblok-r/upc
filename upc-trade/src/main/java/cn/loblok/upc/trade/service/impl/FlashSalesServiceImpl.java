package cn.loblok.upc.trade.service.impl;

import cn.loblok.upc.api.user.feign.UserFeignClient;
import cn.loblok.upc.api.worker.dto.ProductDeliveryMsgDTO;
import cn.loblok.upc.common.base.PageResult;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.enums.MallOrderStatus;
import cn.loblok.upc.common.enums.UserItemSourceType;
import cn.loblok.upc.trade.dto.mall.FlashOrderRequestDTO;
import cn.loblok.upc.trade.dto.mall.FlashSaleDTO;
import cn.loblok.upc.trade.entity.FlashSaleOrders;
import cn.loblok.upc.trade.entity.FlashSales;
import cn.loblok.upc.trade.entity.Products;
import cn.loblok.upc.common.exception.BizException;
import cn.loblok.upc.trade.mapper.FlashSalesMapper;
import cn.loblok.upc.trade.service.FOrdersService;
import cn.loblok.upc.trade.service.FlashSalesService;
import cn.loblok.upc.trade.service.ProductsService;
import cn.loblok.upc.trade.utils.PageConverter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 *   秒杀抢购
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
@Service
@Slf4j
@AllArgsConstructor
public class FlashSalesServiceImpl extends ServiceImpl<FlashSalesMapper, FlashSales> implements FlashSalesService {


    private final FOrdersService ordersService;

    private final ProductsService productsService;

    private final UserFeignClient userFeignClient;

    private final RabbitTemplate rabbitTemplate;


    @Override
    public boolean updateStock(String flashSaleId, int quantity) {
        // 使用乐观锁更新库存
        FlashSales flashSale = this.getById(flashSaleId);
        if (flashSale == null) {
            return false;
        }
        
        int newStock = flashSale.getRemainingStock() + quantity;
        if (newStock < 0) {
            // 库存不足
            return false;
        }
        
        flashSale.setRemainingStock(newStock);
        // 使用updateById会利用MyBatis Plus的乐观锁机制（需要version字段）
        return this.updateById(flashSale);
    }

    @Override
    public Result<String> purchaseFlashSale(Long userId, FlashOrderRequestDTO request) {
        log.info("用户{}正在抢购秒杀活动{}", userId, request.getFlashSaleId());
        // 检查秒杀活动是否存在且有效
        FlashSales flashSale = this.getById(request.getFlashSaleId());

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
        this.updateById(flashSale);

        //扣除积分
        userFeignClient.reduceUserPoints(userId, flashSale.getSalePrice());


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

        // 发送 MQ 消息，任务结束，立即返回给前端
        ProductDeliveryMsgDTO msg = ProductDeliveryMsgDTO.builder()
                .orderId(order.getId())
                .userId(userId)
                .category(product.getCategory().name())
                .productName(product.getName())
                .source(UserItemSourceType.FLASH_SALE.getDescription())
                .deliveryConfig(product.getDeliveryConfig())
                .build();

        rabbitTemplate.convertAndSend("upc.direct.exchange", "mq.route.product_delivery",msg);

        return Result.success("抢购成功，请留意站内信");
    }

    @Override
    public Result<PageResult<FlashSaleDTO>> getFlashSales(int page, int size, String date) {
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

        IPage<FlashSales> result = this.page(flashSalePage, queryWrapper);

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

        return Result.success(PageConverter.toPageResult(dtoPage));
    }
}