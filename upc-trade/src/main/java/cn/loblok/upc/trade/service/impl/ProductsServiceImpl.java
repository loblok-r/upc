package cn.loblok.upc.trade.service.impl;

import cn.loblok.upc.api.user.feign.UserFeignClient;
import cn.loblok.upc.api.worker.dto.ProductDeliveryMsgDTO;
import cn.loblok.upc.common.base.PageResult;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.enums.UserItemSourceType;
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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

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
@AllArgsConstructor
public class ProductsServiceImpl extends ServiceImpl<ProductsMapper, Products> implements ProductsService {


    private final FOrdersService FOrdersService;

    private final UserFeignClient userFeignClient;

    private final RabbitTemplate rabbitTemplate;

    @Override
    // ProductsService.java
    public Products drawRandomPrize() {
        QueryWrapper<Products> query = new QueryWrapper<>();
        query.eq("status", "active")
                .eq("lottery_eligible", true);

        List<Products> prizes = this.list(query);

        if (prizes.isEmpty()) {
            throw new RuntimeException("当前无可抽奖奖品");
        }

        // todo 简单随机，实际可加权重
        Random random = new Random();
        return prizes.get(random.nextInt(prizes.size()));
    }



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
    @Transactional(rollbackFor = Exception.class)
    public Result<String> exchangeProduct(Long userId, ExchangeProducesRequest request) {
        // TODO:校验

        String productId = request.getProductId();
        Products product = this.getById(productId);

        // 检查库存
        if (product.getStock() <= 0) {
            throw new BizException("商品已售完");
        }

        // TODO: 这里应该使用乐观锁更新库存
        // 减少库存
        product.setStock(product.getStock() - 1);
        this.updateById(product);

        //扣除积分
        Result<Void> result = userFeignClient.reduceUserPoints(userId, product.getPointsRequired());

        if(result.getCode() != 200){
            return Result.error(result.getCode(), result.getMsg());
        }

        log.info("扣除用户{}积分成功", userId);

        // 创建订单
        FlashSaleOrders order = new FlashSaleOrders();
        order.setUserId(userId);
        order.setFlashSaleId(null);
        order.setProductId(product.getId());
        order.setPointsSpent(product.getPointsRequired());

        //等待发放
        order.setMallOrderStatus(MallOrderStatus.PENDING_EXTERNAL);
        //没有过期时间
        order.setReserveExpiresAt(null);
        order.setCreatedAt(LocalDateTime.now());

        boolean saved = FOrdersService.save(order);
        if (!saved) {
            throw new BizException("抢购失败，请重试");
        }

        //todo 判断是否是实体商品 发放动作应在订单创建后异步完成，需保证最终一致性。
        // 虚拟商品（算力值，会员，优惠券）发放，事务结束后立即发放

        //异步发放商品

        // 发送 MQ 消息，任务结束，立即返回给前端
        ProductDeliveryMsgDTO msg = ProductDeliveryMsgDTO.builder()
                .orderId(order.getId())
                .userId(userId)
                .source(UserItemSourceType.POINTS_TRADING.getDescription())
                .category(product.getCategory().name())
                .productName(product.getName())
                .deliveryConfig(product.getDeliveryConfig())
                .build();

        rabbitTemplate.convertAndSend("upc.direct.exchange", "mq.route.product_delivery",msg);


        return Result.success("兑换请求已提交，请留意站内信");
    }
}
