package cn.loblok.upc.trade.mapper;


import cn.loblok.upc.trade.entity.FlashSales;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
public interface FlashSalesMapper extends BaseMapper<FlashSales> {

    /**
     * 查询已结束未同步的秒杀活动
     * @return
     */
    @Select("SELECT * FROM flash_sales " +
            "WHERE status = 'ended' " +
            "AND end_time < NOW() " +
            "AND synced_to_product_stock = 0")
    List<FlashSales> selectFinishedUnsynced();

    /**
     * 标记为已同步
     * @param flashSaleId
     */
    @Update("UPDATE flash_sales SET synced_to_product_stock = 1 WHERE id = #{flashSaleId}")
    void markAsSynced(String flashSaleId);

    /**
     * 更新剩余库存
     * @param flashSaleId
     * @param remainingStock
     */
    @Update("UPDATE flash_sales SET remaining_stock = #{remainingStock} WHERE id = #{flashSaleId}")
    void updateRemainingStock(String flashSaleId, int remainingStock);
}
