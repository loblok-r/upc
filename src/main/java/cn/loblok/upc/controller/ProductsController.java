package cn.loblok.upc.controller;

import cn.loblok.upc.dto.LotteryPrizeDTO;
import cn.loblok.upc.dto.PageResult;
import cn.loblok.upc.dto.ProductDTO;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.entity.Products;
import cn.loblok.upc.service.ProductsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
@RestController
@RequestMapping("/api/mall")
@Slf4j
public class ProductsController {

    @Autowired
    private ProductsService productsService;

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
            return dto;
        });

        return Result.success(PageResult.of(dtoPage));
    }


    /**
     * 获取抽奖奖池（仅可抽奖奖品）
     */
    @GetMapping("/lotteryPrizes")
    public Result<List<LotteryPrizeDTO>> getLotteryPrizes() {
        log.info("开始获取抽奖奖品列表");
        QueryWrapper<Products> query = new QueryWrapper<>();
        query.eq("status", "active")
                .eq("lottery_eligible", true)
                .orderByAsc("sort_order");

        List<Products> products = productsService.list(query);

        List<LotteryPrizeDTO> dtos = products.stream()
                .map(this::convertToLotteryPrizeDTO)
                .collect(Collectors.toList());

        log.info("返回抽奖奖品列表：{}", dtos);
        return Result.success(dtos);
    }


    /**
     * 转换为抽奖奖品DTO
     */
    private LotteryPrizeDTO convertToLotteryPrizeDTO(Products product) {
        LotteryPrizeDTO dto = new LotteryPrizeDTO();
        dto.setId(product.getId());
        dto.setTitle(product.getName());
        dto.setSubtitle(product.getDescription());
        dto.setIcon(product.getIconName());
        dto.setColor(product.getDisplayColor());
        dto.setBadge(product.getTag());
        return dto;
    }
}


















