package cn.loblok.upc.modules.mall.controller;

import cn.loblok.upc.common.annotation.CurrentUser;
import cn.loblok.upc.common.base.PageResult;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.modules.mall.dto.ExchangeProducesRequest;
import cn.loblok.upc.modules.mall.dto.ProductDTO;
import cn.loblok.upc.modules.mall.entity.FlashSaleOrders;
import cn.loblok.upc.modules.mall.entity.Products;
import cn.loblok.upc.modules.mall.enums.MallOrderStatus;
import cn.loblok.upc.common.exception.BizException;
import cn.loblok.upc.modules.mall.service.OrdersService;
import cn.loblok.upc.modules.mall.service.PointsService;
import cn.loblok.upc.modules.mall.service.ProductsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * <p>
 *     商城界面
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
@RestController
@RequestMapping("/api/mall")
@Slf4j
@AllArgsConstructor
public class MallController {


    private final ProductsService productsService;


    private final OrdersService ordersService;

    private final PointsService pointsService;

    /**
     * 获取商品列表
     *
     * @param page 页码
     * @param size 页面大小
     * @return 商品列表
     */
    @GetMapping("/products")
    public Result<PageResult<ProductDTO>> getProductList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {


        log.info("获取商品列表，页码：{}，页面大小：{}", page, size);

        IPage<Products> productPage = new Page<>(page, size);
        QueryWrapper<Products> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "active"); // 只查询上架的商品
        queryWrapper.eq("lottery_eligible", false);
        queryWrapper.orderByDesc("created_at");

        IPage<Products> result = productsService.page(productPage, queryWrapper);

        // 转换为 DTO
        IPage<ProductDTO> dtoPage = result.convert(product -> {
            ProductDTO dto = new ProductDTO();
            BeanUtils.copyProperties(product, dto);
            dto.setCategory(product.getCategory().getValue());
            log.info("商品信息dto.getCategory()：{}", dto.getCategory());
            return dto;
        });

        return Result.success(PageResult.of(dtoPage));
    }


    /**
     * 兑换商品
     *
     * @param userId 用户ID
     * @param request 兑换商品请求
     * @return 兑换结果
     */
    @PostMapping("/exchange")
    public Result<String> exchangeProduct(@CurrentUser Long userId, @RequestBody ExchangeProducesRequest request) {

        // TODO:校验

        String productId = request.getProductId();
        Products product = productsService.getById(productId);

        // 检查库存
        if (product.getStock() <= 0) {
            throw new BizException("商品已售完");
        }

        // TODO: 这里应该使用乐观锁更新库存
        // 减少库存
        product.setStock(product.getStock() - 1);
        productsService.updateById(product);

        //扣除积分
        pointsService.reduceUserPoints(userId, product.getPointsRequired());

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

        boolean saved = ordersService.save(order);
        if (!saved) {
            throw new BizException("抢购失败，请重试");
        }

        //todo 判断是否是实体商品 发放动作应在订单创建后异步完成，需保证最终一致性。
        // 虚拟商品（算力值，会员，优惠券）发放，事务结束后立即发放

        //发放商品
        productsService.onOrderSuccess(order, product);

        return Result.success("兑换成功");
    }


}


















