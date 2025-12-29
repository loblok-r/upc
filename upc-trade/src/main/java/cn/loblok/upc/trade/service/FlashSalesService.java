package cn.loblok.upc.trade.service;

import cn.loblok.upc.common.base.PageResult;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.trade.dto.mall.FlashOrderRequestDTO;
import cn.loblok.upc.trade.dto.mall.FlashSaleDTO;
import cn.loblok.upc.trade.dto.mall.PublishFlashSaleDTO;
import cn.loblok.upc.trade.entity.FlashSales;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
public interface FlashSalesService extends IService<FlashSales> {

    /**
     * 秒杀抢购接口
     *
     * @param userId 当前用户ID
     * @param request 抢购请求参数
     * @return 抢购结果
     */
    Result<String> purchaseFlashSale(Long userId, FlashOrderRequestDTO request);

    /**
     * 获取秒杀活动列表
     *
     * @param page 页码
     * @param size 页大小
     * @param date 查询日期
     * @return 秒杀活动列表
     */
    Result<PageResult<FlashSaleDTO>> getFlashSales(int page, int size, String date);

    /**
     * 发布秒杀活动
     *
     * @param publishFlashSaleDTO 秒杀活动信息
     * @return 操作结果
     */
    Result<String> publishFlashSale(PublishFlashSaleDTO publishFlashSaleDTO);

    void syncFlashSaleStockToProduct(String id);
}