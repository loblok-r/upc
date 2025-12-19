package cn.loblok.upc.mapper;

import cn.loblok.upc.modules.member.entity.MembershipOrder;
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

    /**
     * 查询过期未支付的订单
     * @param expireTime 过期时间
     * @return 过期未支付的订单列表
     */
    @Select("SELECT * FROM membership_order WHERE status = 'CREATED' AND created_at < #{expireTime}")
    List<MembershipOrder> selectExpiredUnpaidOrders(LocalDateTime expireTime);

    int closeOrderIfUnpaid(@Param("orderId") Long orderId);
}