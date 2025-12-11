package cn.loblok.upc.entity;

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
 * 点赞记录表
 * </p>
 *
 * @author loblok
 * @since 2025-12-11
 */
@Getter
@Setter
@TableName("like_record")
public class LikeRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 点赞用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 点赞目标类型（如 work, comment）
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 目标ID（如 work.id）
     */
    @TableField("target_id")
    private Long targetId;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
