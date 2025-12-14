package cn.loblok.upc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 会员订单表
 * </p>
 *
 * @author loblok
 * @since 2025-12-14
 */
@Data
@TableName("t_order")
public class TOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单号
     */
    @TableId("order_id")
    private String orderId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 套餐ID
     */
    @TableField("plan_id")
    private String planId;

    /**
     * 支付方式：wechat/alipay
     */
    @TableField("payment_method")
    private String paymentMethod;

    /**
     * 状态：PENDING, SUCCESS, FAILED, EXPIRED
     */
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 支付成功时间
     */
    @TableField("paid_time")
    private LocalDateTime paidTime;
}
