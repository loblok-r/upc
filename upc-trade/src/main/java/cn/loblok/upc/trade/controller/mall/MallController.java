package cn.loblok.upc.trade.controller.mall;

import cn.loblok.upc.common.annotation.CurrentUser;
import cn.loblok.upc.common.base.PageResult;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.trade.dto.mall.ExchangeProducesRequest;
import cn.loblok.upc.trade.dto.mall.ProductDTO;
import cn.loblok.upc.trade.service.FOrdersService;
import cn.loblok.upc.trade.service.ProductsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
@Tag(name = "商城接口", description = "商城接口")
public class MallController {


    private final ProductsService productsService;

    private final FOrdersService fOrdersService;

    /**
     * 获取商品列表
     *
     * @param page 页码
     * @param size 页面大小
     * @return 商品列表
     */
    @GetMapping("/products")
    @Operation(summary = "获取商品列表")
    public Result<PageResult<ProductDTO>> getProductList(
            @RequestParam(value = "page",defaultValue = "1") int page,
            @RequestParam(value="size", defaultValue = "20") int size) {

        return productsService.getProductList(page, size);

    }


    /**
     * 兑换商品
     *
     * @param userId 用户ID
     * @param request 兑换商品请求
     * @return 兑换结果
     */
    @PostMapping("/exchange")
    @Operation(summary = "兑换商品")
    public Result<String> exchangeProduct(@CurrentUser Long userId, @RequestBody ExchangeProducesRequest request) {

       return productsService.exchangeProduct(userId, request);
    }



    /**
     * 查询我的兑换/购买记录
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 页面大小
     * @return 兑换/购买记录列表
     */
    @GetMapping("/orders")
    @Operation(summary = "查询我的购买记录")
    public Result<PageResult<Object>> getMyOrders(
            @CurrentUser Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        //todo 待实现
//        return fOrdersService.getMyOrders(userId, page, size);
        return null;
    }


}


















