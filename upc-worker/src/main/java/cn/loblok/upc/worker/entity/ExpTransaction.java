package cn.loblok.upc.worker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户经验值流水表
 * </p>
 *
 * @author loblok
 * @since 2025-12-02
 */
@Getter
@Setter
@TableName("exp_transaction")
public class ExpTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("tenant_id")
    private String tenantId;

    /**
     * 业务类型: checkin, task, level_up_bonus 等
     */
    @TableField("biz_type")
    private String bizType;

    /**
     * 关联业务ID，如 checkin_record.id
     */
    @TableField("biz_id")
    private String bizId;

    /**
     * 变动值（正为增加）
     */
    @TableField("delta_exp")
    private Integer deltaExp;

    /**
     * 本次变动后的总经验值
     */
    @TableField("balance_after")
    private Long balanceAfter;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
