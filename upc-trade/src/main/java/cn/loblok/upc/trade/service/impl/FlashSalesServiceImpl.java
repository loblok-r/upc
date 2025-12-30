package cn.loblok.upc.trade.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.loblok.upc.api.worker.dto.ProductDeliveryMsgDTO;
import cn.loblok.upc.common.base.PageResult;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.enums.MallOrderStatus;
import cn.loblok.upc.common.enums.UserItemSourceType;
import cn.loblok.upc.common.utils.KeyUtils;
import cn.loblok.upc.trade.dto.mall.FlashOrderRequestDTO;
import cn.loblok.upc.trade.dto.mall.FlashSaleDTO;
import cn.loblok.upc.trade.dto.mall.PublishFlashSaleDTO;
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
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 秒杀抢购
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
@Service
@Slf4j
public class FlashSalesServiceImpl extends ServiceImpl<FlashSalesMapper, FlashSales> implements FlashSalesService {


    private final FOrdersService ordersService;

    private final ProductsService productsService;

    private final RabbitTemplate rabbitTemplate;

    private final RedissonClient redissonClient;

    private final StringRedisTemplate redisTemplate;

    private final DefaultRedisScript<Long> deductStockAndPointsScript;

    private final FlashSalesMapper flashMapper;


    public FlashSalesServiceImpl(
            FOrdersService ordersService,
            ProductsService productsService,
            RabbitTemplate rabbitTemplate,
            RedissonClient redissonClient,
            StringRedisTemplate redisTemplate,
            FlashSalesMapper flashMapper
    ) {

        this.ordersService = ordersService;
        this.productsService = productsService;
        this.rabbitTemplate = rabbitTemplate;
        this.redissonClient = redissonClient;
        this.redisTemplate = redisTemplate;
        this.flashMapper = flashMapper;
        deductStockAndPointsScript = new DefaultRedisScript<>();
        deductStockAndPointsScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/deduct_stock_points.lua")));
        deductStockAndPointsScript.setResultType(Long.class);
    }

    /**
     * 获取秒杀活动列表
     *
     * @param page
     * @param size
     * @param date
     * @return
     */
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


    /**
     * 秒杀抢购
     *
     * @param userId
     * @param request
     * @return
     */
    @Override
    public Result<String> purchaseFlashSale(Long userId, FlashOrderRequestDTO request) {
        log.info("用户{}正在抢购秒杀活动{}", userId, request.getFlashSaleId());

        String lockKey = KeyUtils.buildFlashSaleLockKey(userId, request.getFlashSaleId());
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁：最多等1秒，持有10秒自动释放
            if (lock.tryLock(1, 10, TimeUnit.SECONDS)) {
                // 执行核心抢购
                FlashSaleOrders order = doPurchaseInTransaction(userId, request);

                //TODO 可以异步同步DB，或定时任务同步，DB 乐观锁更新（最终一致性兜底）
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        sendDeliveryMessage(order, userId);
                    }
                });

                return Result.success("抢购成功");
            } else {
                throw new BizException("请勿重复提交");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("获取锁中断");
        } finally {
            lock.unlock(); // Redisson 会自动处理是否持有锁
        }
    }

    /**
     * 秒杀核心逻辑
     *
     * @param userId
     * @param request
     * @return
     */
    @Transactional
    public FlashSaleOrders doPurchaseInTransaction(Long userId, FlashOrderRequestDTO request) {
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
        String stockKey = KeyUtils.buildFlashSaleStockKey(request.getFlashSaleId());
        // 1. 获取字符串结果
        String remainingStockStr = redisTemplate.opsForValue().get(stockKey);

// 2. 解析为 Long（注意判空，防止空指针异常）
        Long remainingStock = (remainingStockStr != null) ? Long.valueOf(remainingStockStr) : 0L;

        if (remainingStock <= 0) {
            throw new BizException("秒杀商品已售完");
        }

        String pointsKey = KeyUtils.buildPointsKey(userId);
        String userFlagKey = KeyUtils.buildFlashSaleUserKey(userId, request.getFlashSaleId());

        long ttlSeconds = Duration.between(LocalDateTime.now(), flashSale.getEndTime()).getSeconds() + 300;
        // 库存扣减使用乐观锁（并发超卖）
        Long result = (Long) redisTemplate.execute(
                deductStockAndPointsScript,
                List.of(stockKey, pointsKey, userFlagKey),
                String.valueOf(flashSale.getSalePrice()),
                "1",  // 扣1件库存
                ttlSeconds // 防重1小时
        );
        if (result == null || result == 0L) {
            throw new BizException("手慢啦，商品已被抢光");
        }
        if (result == -1L) {
            throw new BizException("积分不足，快去赚积分吧！");
        }
        if (result == -2L) {
            throw new BizException("您已参与过本次秒杀");
        }


        // 创建订单
        FlashSaleOrders order = new FlashSaleOrders();
        order.setUserId(userId);
        order.setPointsSpent(flashSale.getSalePrice());
        order.setFlashSaleId(request.getFlashSaleId());
        order.setProductId(flashSale.getProductId());
        //等待发放
        order.setMallOrderStatus(MallOrderStatus.PENDING_EXTERNAL);
        //没有过期时间
        order.setReserveExpiresAt(null);
        order.setCreatedAt(LocalDateTime.now());

        if (!ordersService.save(order)) {
            log.error("订单创建失败，userId={}, flashSaleId={}", userId, request.getFlashSaleId());
            // 1. 记录到“待补偿表” 或
            // 2. 直接抛异常，由上层告警 + 对账任务处理
            throw new BizException("系统繁忙，请稍后查看订单");
        }
        return order;
    }


    /**
     * 发送 MQ 秒杀成功消息
     *
     * @param order
     * @param userId
     */
    private void sendDeliveryMessage(FlashSaleOrders order, Long userId) {

        String productId = order.getProductId();
        Products product = productsService.getById(productId);
        String bizId = IdUtil.randomUUID();
        CorrelationData correlationData = new CorrelationData(bizId);
        // 发送 MQ 消息，任务结束，立即返回给前端
        ProductDeliveryMsgDTO msg = ProductDeliveryMsgDTO.builder()
                .orderId(order.getId())
                .userId(userId)
                .category(product.getCategory().name())
                .productName(product.getName())
                .source(UserItemSourceType.FLASH_SALE.getDescription())
                .deliveryConfig(product.getDeliveryConfig())
                .build();
        rabbitTemplate.convertAndSend(
                "upc.direct.exchange",
                "mq.route.product_delivery",
                msg,
                message -> {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                },
                correlationData
        );
    }


    /**
     * 发布秒杀活动
     *
     * @param dto
     * @return
     */
    @Override
    public Result<String> publishFlashSale(PublishFlashSaleDTO dto) {
        log.info("开始发布秒杀活动: {}", dto);
        // todo 校验活动配置
        //预占库存
        productsService.deductStock(dto.getId(), dto.getTotalStock());
        // 将库存写入 Redis
        String stockKey = KeyUtils.buildFlashSaleStockKey(dto.getId());
        redisTemplate.opsForValue().set(
                stockKey,
                String.valueOf(dto.getTotalStock()),
                Duration.between(LocalDateTime.now(), dto.getEndTime()).getSeconds(),
                TimeUnit.SECONDS
        );
        // 更新 DB 状态为 "active"
        this.updateStatus(dto.getId(), "active");
        return Result.success(null);
    }

    /**
     * 同步库存到商品
     *
     * @param flashSaleId
     */
    @Override
    @Transactional
    public void syncFlashSaleStockToProduct(String flashSaleId) {
        FlashSales flashSale = flashMapper.selectById(flashSaleId);
        if (flashSale == null || flashSale.getSyncedToProductStock()) {return;}

        // 1. 从 Redis 获取剩余库存（更准确！因为订单可能有失败/取消）
        String stockKey = KeyUtils.buildFlashSaleStockKey(flashSaleId);
        String remainingStr = (String) redisTemplate.opsForValue().get(stockKey);
        int remainingStock = (remainingStr != null) ? Integer.parseInt(remainingStr) : 0;

        // 2. 【归还】未售出的库存到 products.stock
        boolean updated = productsService.addStock(flashSale.getProductId(), remainingStock);
        if (!updated) {
            log.error("归还库存失败，flashSaleId={}, remaining={}", flashSaleId, remainingStock);
            throw new BizException("库存归还异常");
        }

        // 3. 同时更新 Redis 普通商品库存（因为总库存增加了！）
        String productStockKey = KeyUtils.buildProductStockKey(flashSale.getProductId());
        redisTemplate.opsForValue().increment(productStockKey, remainingStock);

        // 4. 更新 flash_sales 表记录剩余量（用于对账）
        flashMapper.updateRemainingStock(flashSaleId, remainingStock);

        // 5. 标记已同步
        flashMapper.markAsSynced(flashSaleId);
    }


    /**
     * 更新活动状态
     *
     * @param id
     * @param active
     */
    private void updateStatus(String id, String active) {
        this.update(new UpdateWrapper<FlashSales>().eq("id", id).set("status", active));
    }


}