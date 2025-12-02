package cn.loblok.upc.entity;

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
    private String templateCode;

    @TableField("name")
    private String name;

    @TableField("type")
    private Byte type;

    @TableField("discount_value")
    private BigDecimal discountValue;

    @TableField("min_order_amount")
    private BigDecimal minOrderAmount;

    @TableField("total_stock")
    private Integer totalStock;

    @TableField("per_user_limit")
    private Byte perUserLimit;

    @TableField("valid_days")
    private Integer validDays;

    @TableField("fixed_valid_from")
    private LocalDateTime fixedValidFrom;

    @TableField("fixed_valid_to")
    private LocalDateTime fixedValidTo;

    @TableField("status")
    private Byte status;

    @TableField("biz_trigger")
    private String bizTrigger;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
