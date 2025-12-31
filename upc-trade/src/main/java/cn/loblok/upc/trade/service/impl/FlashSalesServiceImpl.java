package cn.loblok.upc.trade.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.loblok.rabbit.util.rabbit.constants.MQConstants;
import cn.loblok.upc.api.worker.dto.SeckillOrderCreateDTO;
import cn.loblok.upc.common.base.PageResult;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.utils.KeyUtils;
import cn.loblok.upc.trade.dto.mall.FlashOrderRequestDTO;
import cn.loblok.upc.trade.dto.mall.FlashSaleDTO;
import cn.loblok.upc.trade.dto.mall.PublishFlashSaleDTO;
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
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.util.concurrent.RateLimiter;
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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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


    //本地限流器：单机每秒只允许 100 个请求进入，防止 Tomcat 线程耗尽
    private final RateLimiter localLimiter = RateLimiter.create(100.0);

    // 本地售罄标记：如果 Redis 提示没库存了，本地直接拦截，不再访问 Redis
    private final Map<String, Boolean> stockOutMap = new ConcurrentHashMap<>();

    // 本地活动信息缓存：5秒自动刷新，避免高并发下频繁查 DB
    private final Cache<String, FlashSales> flashSaleLocalCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .maximumSize(100)
            .build();



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
        String flashId = request.getFlashSaleId();
        // 微秒级拦截，不产生网络 IO
        if (!localLimiter.tryAcquire()) {
            throw new BizException("抢购人数过多，请稍后再试"); // 快速失败，保护系统
        }

        //本地售罄快照
        if (Boolean.TRUE.equals(stockOutMap.get(flashId))) {
            throw new BizException("商品已售罄 (Local)");
        }

        //本地缓存校验活动状态
        // 使用 Optional 包装或判空，防止无效 ID 穿透
        FlashSales flashSale = flashSaleLocalCache.get(flashId, id -> {
            FlashSales dbSale = this.getById(id);
            // 如果数据库也没有，可以返回一个特殊状态的对象，防止频繁回源
            return dbSale;
        });

        if (flashSale == null || !"active".equals(flashSale.getStatus())) {
            throw new BizException("活动不存在或已结束");
        }

        //时间校验
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(flashSale.getStartTime())) {
            throw new BizException("秒杀尚未开始");
        }

        //分布式锁，防止同一个用户通过脚本开启多个线程刷单
        String lockKey = KeyUtils.buildFlashSaleLockKey(userId, flashId);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁，最多等1秒，持有10秒自动释放
            if (lock.tryLock(500, 5000, TimeUnit.MILLISECONDS)) {

                // 执行核心 Lua 扣减
                return executeSeckillLogic(userId, flashSale);

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
     * 核心秒杀逻辑
     *
     * @param userId
     * @param flashSale
     * @return
     */
    private Result<String> executeSeckillLogic(Long userId, FlashSales flashSale) {
        String flashId = flashSale.getId();
        String stockKey = KeyUtils.buildFlashSaleStockKey(flashId);
        String pointsKey = KeyUtils.buildPointsKey(userId);
        String userFlagKey = KeyUtils.buildFlashSaleUserKey(userId, flashId);

        // --- 第五层：Redis Lua 原子扣减 ---
        Long result = redisTemplate.execute(
                deductStockAndPointsScript,
                List.of(stockKey, pointsKey, userFlagKey),
                String.valueOf(flashSale.getSalePrice()),
                "1",
                "3600"
        );

        //标记本地售罄，拦截后续流量
        if (result == 0L) {
            stockOutMap.put(flashId, true);
            throw new BizException("手慢啦，商品已抢光");
        }
        if (result == -1L) throw new BizException("积分不足");
        if (result == -2L) throw new BizException("您已参加过本次活动");

        // MQ 异步落库
        // 发消息到 MQ，让订单服务慢慢消费
        sendOrderCreateMessage(userId, flashSale);
        return Result.success("抢购成功，请稍后在订单列表中查看");
    }

    /**
     * 发送下单消息
     *
     * @param userId
     * @param flashSale
     */
    private void sendOrderCreateMessage(Long userId, FlashSales flashSale) {
        SeckillOrderCreateDTO msg = SeckillOrderCreateDTO.builder()
                .userId(userId)
                .flashSaleId(flashSale.getId())
                .productId(flashSale.getProductId())
                .payPoints(flashSale.getSalePrice())
                .traceId(IdUtil.fastSimpleUUID())
                .build();

        String bizId = IdUtil.randomUUID();
        CorrelationData correlationData = new CorrelationData(bizId);

        rabbitTemplate.convertAndSend(
                MQConstants.EXCHANGE_NAME,
                MQConstants.ROUTE_ORDER_CREATE,
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

        // 1. 从 Redis 获取剩余库存
        String stockKey = KeyUtils.buildFlashSaleStockKey(flashSaleId);
        String remainingStr = (String) redisTemplate.opsForValue().get(stockKey);
        int remainingStock = (remainingStr != null) ? Integer.parseInt(remainingStr) : 0;

        // 2. 未售出的库存到 products.stock
        boolean updated = productsService.addStock(flashSale.getProductId(), remainingStock);
        if (!updated) {
            log.error("归还库存失败，flashSaleId={}, remaining={}", flashSaleId, remainingStock);
            throw new BizException("库存归还异常");
        }

        // 3. 同时更新 Redis 普通商品库存
        String productStockKey = KeyUtils.buildProductStockKey(flashSale.getProductId());
        redisTemplate.opsForValue().increment(productStockKey, remainingStock);

        // 4. 更新 flash_sales 表记录剩余量,用于对账
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