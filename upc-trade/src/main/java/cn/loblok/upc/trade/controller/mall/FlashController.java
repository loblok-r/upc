package cn.loblok.upc.trade.controller.mall;

import cn.loblok.upc.api.user.feign.UserFeignClient;
import cn.loblok.upc.common.annotation.CurrentUser;
import cn.loblok.upc.trade.dto.mall.FlashOrderRequestDTO;
import cn.loblok.upc.trade.dto.mall.FlashSaleDTO;
import cn.loblok.upc.common.base.PageResult;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.trade.service.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *     秒杀订单
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
@RestController
@RequestMapping("/api/mall/flash")
@AllArgsConstructor
@Slf4j
public class FlashController {


    private final FlashSalesService flashSalesService;


    /**
     * 获取秒杀活动列表
     *
     * @param page 页码
     * @param size 页面大小
     * @return 秒杀活动列表
     */
    @GetMapping("/list")
    public Result<PageResult<FlashSaleDTO>> getFlashSales(
            @RequestParam(value = "page",defaultValue = "1") int page,
            @RequestParam(value = "size",defaultValue = "10") int size,
            @RequestParam(value ="date",required = false) String date) {  // 添加日期参数

         return flashSalesService.getFlashSales(page, size, date);
    }

    /**
     * 秒杀抢购接口
     *
     * @param userId 当前用户ID
     * @param request 请求参数
     * @return 抢购结果
     */
    @PostMapping("/grab")
    public Result<String> purchaseFlashSale(@CurrentUser Long userId, 
                                          @Valid @RequestBody FlashOrderRequestDTO request) {

            return flashSalesService.purchaseFlashSale(userId, request);
    }


}