package cn.loblok.upc.mapper;

import cn.loblok.upc.entity.MembershipOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author loblok
 * @since 2025-12-03
 */
@Mapper
public interface MembershipOrderMapper extends BaseMapper<MembershipOrder> {
    
    /**
     * 根据订单号查询订单
     * @param orderNo 订单号
     * @return 订单信息
     */
    @Select("SELECT * FROM membership_order WHERE order_no = #{orderNo}")
    MembershipOrder selectByOrderNo(@Param("orderNo") String orderNo);

    List<MembershipOrder> selectExpiredUnpaidOrders(LocalDateTime expireTime);

    int closeOrderIfUnpaid(@Param("orderId") Long orderId);
}