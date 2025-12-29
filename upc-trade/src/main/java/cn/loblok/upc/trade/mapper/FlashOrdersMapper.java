package cn.loblok.upc.trade.mapper;


import cn.loblok.upc.trade.entity.FlashSaleOrders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
public interface FlashOrdersMapper extends BaseMapper<FlashSaleOrders> {


    /**
     * 统计成功订单数
     *
     * @param flashSaleId
     * @return
     */

    @Select("SELECT COUNT(*) FROM orders WHERE flash_sale_id = #{flashSaleId} AND order_status IN (2,5)")
    int countSuccessfulOrders(String flashSaleId);

}
