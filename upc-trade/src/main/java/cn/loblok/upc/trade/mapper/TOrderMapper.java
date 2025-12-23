package cn.loblok.upc.trade.mapper;


import cn.loblok.upc.trade.entity.TOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author loblok
 * @since 2025-12-14
 */
public interface TOrderMapper extends BaseMapper<TOrder> {


    /**
     * 查询过期未支付的订单
     * @param expireTime 过期时间
     * @return 过期未支付的订单列表
     */
    @Select("SELECT * FROM t_order WHERE status = 'PAYING' AND create_time < #{expireTime}")
    List<TOrder> selectExpiredUnpaidOrders(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 关闭未支付的订单
     * @param
     * @return 更新影响的行数
     */
    @Update("UPDATE t_order SET status = 'CLOSED', update_time = NOW() WHERE id = #{orderId} AND status = 'PAYING'")
    int closeOrderIfUnpaid(@Param("orderId") String orderId);
}
