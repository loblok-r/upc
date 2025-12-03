package cn.loblok.upc.entity;

import cn.loblok.upc.enums.MembershipOrderStatus;
import com.baomidou.mybatisplus.annotation.*;

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
 * @since 2025-12-03
 */
@Getter
@Setter
@TableName("membership_order")
public class MembershipOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商户订单号（如 MEM20250603123456）
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 会员类型
     */
    @TableField("type")
    private String type;

    /**
     * 有效期天数（30 / -1）
     */
    @TableField("duration_days")
    private Integer durationDays;

    /**
     * 支付金额（单位：元）
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 状态
     */
    @TableField("status")
    private MembershipOrderStatus status;

    /**
     * 支付渠道（wechat / alipay）
     */
    @TableField("pay_channel")
    private String payChannel;

    /**
     * 第三方交易号（微信/支付宝返回）
     */
    @TableField("transaction_id")
    private String transactionId;

    /**
     * 支付成功时间
     */
    @TableField("paid_at")
    private LocalDateTime paidAt;

    /**
     * 会员过期时间（永久会员为 NULL）
     */
    @TableField("expired_at")
    private LocalDateTime expiredAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("closed_at")
    private LocalDateTime closedAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
