package cn.loblok.upc.modules.mall.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 优惠券发放记录表
 * </p>
 *
 * @author loblok
 * @since 2025-12-02
 */
@Getter
@Setter
@TableName("coupon_issue_log")
public class CouponIssueLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("template_code")
    private String templateCode;

    /**
     * 业务类型: REGISTER/SIGN/INVITE/ACTIVITY
     */
    @TableField("biz_type")
    private String bizType;

    /**
     * 业务唯一ID，用于幂等，如 user_1001_register
     */
    @TableField("biz_id")
    private String bizId;

    /**
     * 0=成功, 1=失败
     */
    @TableField("status")
    private Byte status;

    @TableField("error_msg")
    private String errorMsg;

    @TableField(value = "issued_at",fill = FieldFill.INSERT)
    private LocalDateTime issuedAt;
}
