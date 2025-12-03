package cn.loblok.upc.mapper;

import cn.loblok.upc.entity.CouponIssueLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 优惠券发放记录表 Mapper 接口
 * </p>
 *
 * @author loblok
 * @since 2025-12-02
 */
@Mapper
public interface CouponIssueLogMapper extends BaseMapper<CouponIssueLog> {
    
    /**
     * 根据业务ID检查是否已发放过优惠券
     * @param bizId 业务唯一ID
     * @return 如果已存在返回1，否则返回null
     */
    @Select("SELECT 1 FROM coupon_issue_log WHERE biz_id = #{bizId} LIMIT 1")
    Integer existsByBizId(@Param("bizId") String bizId);
}