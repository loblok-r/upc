package cn.loblok.upc.controller;

import cn.loblok.upc.annotation.CurrentUser;
import cn.loblok.upc.dto.DrawResultDTO;
import cn.loblok.upc.dto.LotteryPrizeDTO;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.entity.Products;
import cn.loblok.upc.enums.ProductStatus;
import cn.loblok.upc.service.LotteryService;
import cn.loblok.upc.service.ProductsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 抽奖控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/lottery")
@AllArgsConstructor
public class LotteryController {



    private final LotteryService lotteryService;


    private final ProductsService productsService;


    /**
     * 获取抽奖奖池
     */
    @GetMapping("/lotteryPrizes")
    public Result<List<LotteryPrizeDTO>> getLotteryPrizes() {
        log.info("开始获取抽奖奖品列表");
        QueryWrapper<Products> query = new QueryWrapper<>();
        query.eq("status", ProductStatus.ACTIVE)
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


    /**
     * 抽奖
     */
    @PostMapping("/draw")
    public Result<DrawResultDTO> drawLottery(@CurrentUser Long userId) {
        log.info("收到抽奖请求");
        return  lotteryService.draw(userId);
    }
}