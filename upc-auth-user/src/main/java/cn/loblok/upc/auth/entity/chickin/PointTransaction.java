package cn.loblok.upc.auth.entity.chickin;

import cn.loblok.upc.common.enums.BizType;
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
 * 用户积分流水表
 * </p>
 *
 * @author loblok
 * @since 2025-12-01
 */
@Getter
@Setter
@TableName("point_transaction")
public class PointTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("tenant_id")
    private String tenantId;

    /**
     * 业务类型: checkin, task, refund, exchange 等
     */
    @TableField("biz_type")
    private BizType bizType;

    /**
     * 关联业务主键，如 checkin_record.id
     */
    @TableField("biz_id")
    private String bizId;

    /**
     * 变动值（正为增加，负为扣除）
     */
    @TableField("delta_points")
    private Integer deltaPoints;

    /**
     * 本次变动后的总积分余额
     */
    @TableField("balance_after")
    private Long balanceAfter;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
