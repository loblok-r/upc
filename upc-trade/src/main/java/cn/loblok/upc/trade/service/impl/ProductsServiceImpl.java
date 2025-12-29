package cn.loblok.upc.trade.service.impl;

import cn.loblok.upc.api.worker.dto.ProductDeliveryMsgDTO;
import cn.loblok.upc.common.base.PageResult;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.enums.UserItemSourceType;
import cn.loblok.upc.common.utils.KeyUtils;
import cn.loblok.upc.trade.dto.mall.ExchangeProducesRequest;
import cn.loblok.upc.trade.dto.mall.ProductDTO;
import cn.loblok.upc.trade.entity.FlashSaleOrders;
import cn.loblok.upc.trade.entity.Products;
import cn.loblok.upc.common.enums.MallOrderStatus;
import cn.loblok.upc.common.exception.BizException;
import cn.loblok.upc.trade.mapper.ProductsMapper;
import cn.loblok.upc.trade.service.FOrdersService;
import cn.loblok.upc.trade.service.ProductsService;
import cn.loblok.upc.trade.utils.PageConverter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>
 *     商品服务
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
@Service
@Slf4j
public class ProductsServiceImpl extends ServiceImpl<ProductsMapper, Products> implements ProductsService {



    private  final FOrdersService fOrdersService;


    private  final RabbitTemplate rabbitTemplate;


    private  final RedisTemplate<String, String> redisTemplate;

    private final DefaultRedisScript<Long> deductStockAndPointsScript;


    /**
     * 项目启动时初始化缓存
     */
    @PostConstruct
    public void initLotteryPrizeCacheOnStartup() {
        refreshLotteryPrizeCache();
        initProductStockCache();
    }

    public ProductsServiceImpl(
            FOrdersService fOrdersService,
            RabbitTemplate rabbitTemplate,
            RedisTemplate<String, String> redisTemplate
    ) {
        this.fOrdersService = fOrdersService;
        this.rabbitTemplate = rabbitTemplate;
        this.redisTemplate = redisTemplate;

        this.deductStockAndPointsScript = new DefaultRedisScript<>();
        this.deductStockAndPointsScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/deduct_stock_points.lua")));
        this.deductStockAndPointsScript.setResultType(Long.class);
    }

    /**
     * 刷新抽奖奖品缓存
     */
    private void refreshLotteryPrizeCache() {
        QueryWrapper<Products> query = new QueryWrapper<>();
        query.eq("status", "active").eq("lottery_eligible", true);

        List<Products> prizes = baseMapper.selectList(query);

        redisTemplate.delete("lottery:prizes:blue");
        redisTemplate.delete("lottery:prizes:purple");
        redisTemplate.delete("lottery:prizes:yellow");

        for (Products p : prizes) {
            String color = p.getDisplayColor();
            String key = switch (color) {
                case "text-blue-400" -> "lottery:prizes:blue";
                case "text-purple-400" -> "lottery:prizes:purple";
                case "text-yellow-400" -> "lottery:prizes:yellow";
                default -> null;
            };
            if (key != null) {
                redisTemplate.opsForSet().add(key, p.getId().toString());
            }
        }
        log.info("抽奖奖品缓存初始化完成，共加载 {} 个奖品", prizes.size());
    }

    /**
     * 初始化商品库存缓存
     */

    private void initProductStockCache() {
        List<Products> products = this.list(new QueryWrapper<Products>().eq("status", "active"));
        for (Products p : products) {
            String key = KeyUtils.buildProductStockKey(p.getId().toString());
            redisTemplate.opsForValue().set(key, String.valueOf(p.getStock()));
        }
    }

    @Override
    public Products drawRandomPrize() {

        // 限定重试 3 次，防止死循环
        for (int i = 0; i < 3; i++) {
            String selectedColorKey = selectColorByProbability();
            String prizeIdStr = (String) redisTemplate.opsForSet().randomMember(selectedColorKey);

            if (prizeIdStr != null) {
                return this.getById(Long.valueOf(prizeIdStr));
            }
            log.warn("奖池 {} 为空，尝试重试第 {} 次", selectedColorKey, i + 1);
        }
        // 如果重试都失败，抛出异常或返回兜底奖品
        throw new BizException("奖池暂时无货，请稍后再抽");
    }

    /**
     * 按概率选择颜色
     *
     * @return 颜色
     */
    private String selectColorByProbability() {
        log.info("开始选择颜色");
        int maxAttempts = 10;
        for (int i = 0; i < maxAttempts; i++) {
            double rand = ThreadLocalRandom.current().nextDouble();
            String key;
            if (rand < 0.5) {
                key = "lottery:prizes:blue";
            } else if (rand < 0.8) {
                key = "lottery:prizes:purple";
            } else {
                key = "lottery:prizes:yellow";
            }

            // 检查该颜色是否有奖品
            Long size = redisTemplate.opsForSet().size(key);
            if (size != null && size > 0) {
                return key;
            }
            // 否则继续尝试
        }
        throw new BizException("当前无可抽奖奖品");
    }


    /**
     * 获取商品列表
     *
     * @param page 页码
     * @param size 页面大小
     * @return 商品列表
     */

    @Override
    public Result<PageResult<ProductDTO>> getProductList(int page, int size) {
        log.info("获取商品列表，页码：{}，页面大小：{}", page, size);

        IPage<Products> productPage = new Page<>(page, size);
        QueryWrapper<Products> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "active"); // 只查询上架的商品
        queryWrapper.eq("lottery_eligible", false);
        queryWrapper.orderByDesc("created_at");

        IPage<Products> result = this.page(productPage, queryWrapper);
        // 转换为 DTO
        IPage<ProductDTO> dtoPage = result.convert(product -> {
            ProductDTO dto = new ProductDTO();
            BeanUtils.copyProperties(product, dto);
            dto.setCategory(product.getCategory().getValue());
            log.info("商品信息dto.getCategory()：{}", dto.getCategory());
            return dto;
        });

        return Result.success(PageConverter.toPageResult(dtoPage));
    }



    @Override
    public Result<String> exchangeProduct(Long userId, ExchangeProducesRequest request) {
        // 校验参数、权限等...
        String productId = request.getProductId();
        Products product = this.getById(productId);

        if (product == null || product.getStock() <= 0) {
            throw new BizException("商品不存在或已售罄");
        }

        int quantity = request.getQuantity() != 0 ? request.getQuantity() : 1;


        String stockKey = KeyUtils.buildProductStockKey(productId);
        String pointsKey = KeyUtils.buildPointsKey(userId);

        // 执行和秒杀一样的 Lua 扣减
        Long result = (Long) redisTemplate.execute(
                deductStockAndPointsScript, // 复用秒杀的 script
                List.of(stockKey, pointsKey),
                String.valueOf(product.getPointsRequired() * quantity),
                String.valueOf(quantity),
                "3600"
        );

        if (result == 0L) {throw new BizException("库存不足");}
        if (result == -1L) {throw new BizException("积分不足");}
        if (result == -2L) {throw new BizException("请勿重复提交");}

        FlashSaleOrders order = performExchangeInTransaction(userId, product, quantity);

        // 事务成功后，发 MQ
        sendMsgAfterCommit(userId, order, product,quantity);

        return Result.success("兑换请求已提交，请留意站内信");
    }

    /**
     * 兑换商品
     *
     * @param userId 用户ID
     * @return 兑换结果
     */
    @Transactional(rollbackFor = Exception.class)
    public FlashSaleOrders performExchangeInTransaction(Long userId, Products product, int quantity) {
        // 2. 创建订单
        FlashSaleOrders order = new FlashSaleOrders();
        order.setUserId(userId);
        order.setProductId(product.getId());
        order.setPointsSpent(product.getPointsRequired() * quantity);
        order.setMallOrderStatus(MallOrderStatus.PENDING_EXTERNAL);
        order.setCreatedAt(LocalDateTime.now());

        boolean saved = fOrdersService.save(order);
        if (!saved) {
            throw new BizException("订单创建失败");
        }

        return order;
    }

    private void sendMsgAfterCommit(Long userId, FlashSaleOrders order, Products product, int quantity) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                ProductDeliveryMsgDTO msg = ProductDeliveryMsgDTO.builder()
                        .orderId(order.getId())
                        .userId(userId)
                        .source(UserItemSourceType.POINTS_TRADING.getDescription())
                        .category(product.getCategory().name())
                        .productName(product.getName())
                        .deliveryConfig(product.getDeliveryConfig())
                        .build();
                rabbitTemplate.convertAndSend("upc.direct.exchange", "mq.route.product_delivery", msg);
            }
        });
    }

    @Override
    @Transactional
    public boolean deductStock(String productId, int quantity) {
        Products product = this.getById(productId);
        if (product == null || product.getStock() < quantity) {
            return false;
        }
        // 扣减
        product.setStock(product.getStock() - quantity);
        // MP 会自动处理 version++
        return this.updateById(product); // 如果 version 冲突，返回 false
    }

    @Override
    @Transactional
    public boolean addStock(String productId, int quantity) {
        Products product = this.getById(productId);
        if (product == null) {return false;}
        product.setStock(product.getStock() + quantity);
        return this.updateById(product);
    }
}
