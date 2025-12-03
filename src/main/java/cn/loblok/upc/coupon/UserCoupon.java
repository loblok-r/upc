package cn.loblok.upc.coupon;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
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
 * 用户优惠券实例表
 */
@Getter
@Setter
@TableName("user_coupon")
public class UserCoupon implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("template_id")
    private Long templateId;

    @TableField("template_code")
    private String templateCode;

    @TableField("status")
    private Byte status;

    @TableField("order_id")
    private Long orderId;

    @TableField("received_at")
    private LocalDateTime receivedAt;

    @TableField("used_at")
    private LocalDateTime usedAt;

    @TableField("expired_at")
    private LocalDateTime expiredAt;
}
