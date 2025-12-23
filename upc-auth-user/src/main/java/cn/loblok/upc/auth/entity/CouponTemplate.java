package cn.loblok.upc.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author loblok
 * @since 2025-12-02
 */

/**
 * 优惠券模板
 */
@Getter
@Setter
@TableName("coupon_template")
public class CouponTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("template_code")
    private String templateCode; // 优惠券模板编号 如 'HD_One_Time_Experience'

    @TableField("activity_code")
    private String activityCode; // 活动编号

    @TableField("name")
    private String name; // 优惠券名称  "HD一次体验券"

    @TableField("type")
    private Byte type; // 优惠券类型  0=折扣券, 1=权益券, 2=积分券

    /**
     * 权益特征
     *  HD_ONE_TIME,HD一次体验券
     *  PRO_ONE_TIME,PRO一次体验券
     *  MONEDAY,MONEDAY 一天会员体验券
     */
    @TableField("entitlement_feature")
    private String entitlementFeature;

    @TableField("discount_value")
    private BigDecimal discountValue; //-- 减10.00 / 打8折(0.8) / 送100积分

    @TableField("min_order_amount")
    private BigDecimal minOrderAmount; // -- 满多少可用（现金券）

    @TableField("total_stock")
    private Integer totalStock; // 总库存

    @TableField("per_user_limit")
    private Byte perUserLimit; // 每个用户限领张数

    @TableField("valid_days")
    private Integer validDays; // 有效天数

    @TableField("fixed_valid_from")
    private LocalDateTime fixedValidFrom;

    @TableField("fixed_valid_to")
    private LocalDateTime fixedValidTo;

    @TableField("status")
    private Byte status;    // 0=停用, 1=启用

    @TableField("biz_trigger")
    private String bizTrigger; // 业务触发条件: register/checkin/level_up

    @TableField("created_at")
    private LocalDateTime createdAt;
}
