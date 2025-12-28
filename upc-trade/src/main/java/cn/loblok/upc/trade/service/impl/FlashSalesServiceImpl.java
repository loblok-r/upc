package cn.loblok.upc.trade.service.impl;

import cn.loblok.upc.api.user.feign.UserFeignClient;
import cn.loblok.upc.api.worker.dto.ProductDeliveryMsgDTO;
import cn.loblok.upc.common.base.PageResult;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.enums.MallOrderStatus;
import cn.loblok.upc.common.enums.UserItemSourceType;
import cn.loblok.upc.common.utils.RedisUtils;
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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

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


    @Autowired
    private FOrdersService ordersService;

    @Autowired
    private ProductsService productsService;

    @Autowired
    private  UserFeignClient userFeignClient;

    @Autowired
    private  RabbitTemplate rabbitTemplate;

    @Autowired
    private  RedissonClient redissonClient;

    @Autowired
    private  RedisTemplate redisTemplate;

    private final DefaultRedisScript<Long> deductStockScript;


    public FlashSalesServiceImpl() {
        deductStockScript = new DefaultRedisScript<>();
        deductStockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/deduct_stock.lua")));
        deductStockScript.setResultType(Long.class);
    }

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

    public boolean tryDeductStock(String flashSaleId) {
        String stockKey = "flash:stock:" + flashSaleId;
        Long result = (Long)redisTemplate.execute(deductStockScript,
                Collections.singletonList(stockKey),
                ""); // 可传 userId 做更细粒度控制

        return result != null && result >= 0;
    }

    /**
     * 秒杀抢购
     * @param userId
     * @param request
     * @return
     */
    @Override
    public Result<String> purchaseFlashSale(Long userId, FlashOrderRequestDTO request) {
        log.info("用户{}正在抢购秒杀活动{}", userId, request.getFlashSaleId());

        String lockKey = RedisUtils.buildFlashSaleLockKey(userId, request.getFlashSaleId());
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁：最多等1秒，持有10秒自动释放
            if (lock.tryLock(1, 10, TimeUnit.SECONDS)) {
                // 执行核心抢购
                FlashSaleOrders order = doPurchaseInTransaction(userId, request);

                //TODO 可以异步同步DB，或定时任务同步，DB 乐观锁更新（最终一致性兜底）
                // 事务成功提交后，发送MQ,此处也可以用监听器
                sendDeliveryMessage(order, userId);

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
     * 发送 MQ 秒杀成功消息
     * @param order
     * @param userId
     */
    private void sendDeliveryMessage(FlashSaleOrders order, Long userId) {

        String productId = order.getProductId();
        Products product = productsService.getById(productId);
        // 发送 MQ 消息，任务结束，立即返回给前端
        ProductDeliveryMsgDTO msg = ProductDeliveryMsgDTO.builder()
                .orderId(Long.parseLong(order.getId()))
                .pointsSpent(order.getPointsSpent())
                .userId(userId)
                .category(product.getCategory().name())
                .productName(product.getName())
                .source(UserItemSourceType.FLASH_SALE.getDescription())
                .deliveryConfig(product.getDeliveryConfig())
                .build();
        rabbitTemplate.convertAndSend("upc.direct.exchange", "mq.route.product_delivery",msg);
    }


    /**
     * 秒杀核心逻辑
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
        if (flashSale.getRemainingStock() <= 0) {
            throw new BizException("秒杀商品已售完");
        }

        // 库存扣减使用乐观锁（并发超卖）

        if (!tryDeductStock(request.getFlashSaleId())) {
            throw new BizException("手慢了，商品已售罄");
        }

        try{
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
            return order;
        } catch (Exception e) {
            try {
                redisTemplate.opsForValue().increment("flash:stock:" + request.getFlashSaleId());
            } catch (Exception ex) {
                // 降级：记录日志 + 发告警 + 后续对账修复
                log.error("库存回滚失败，flashSaleId={}", request.getFlashSaleId(), ex);
                // todo 发送补偿 MQ（当前架构可暂不加）
            }
            throw new BizException("抢购失败，请重试");
        }
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

    @Override
    public Result<String> publishFlashSale(PublishFlashSaleDTO dto) {
        log.info("开始发布秒杀活动: {}", dto);
        // todo 校验活动配置
        // 将库存写入 Redis
        String stockKey = "flash:stock:" + dto.getId();
        redisTemplate.opsForValue().set(stockKey, dto.getTotalStock(),
                Duration.between(LocalDateTime.now(), dto.getEndTime()));
        // 更新 DB 状态为 "active"
        this.updateStatus(dto.getId(), "active");
        return Result.success(null);
    }

    /**
     * 更新活动状态
     * @param id
     * @param active
     */
    private void updateStatus(Long id, String active) {
        this.update(new UpdateWrapper<FlashSales>().eq("id", id).set("status", active));
    }
}