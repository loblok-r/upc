package cn.loblok.upc.modules.community.entity;

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
 * 评论表
 * </p>
 *
 * @author loblok
 * @since 2025-12-11
 */
@Getter
@Setter
@TableName("comment")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 评论者ID
     */
    @TableField("user_id")
    private Long userId;

    @TableField("target_type")
    private String targetType;

    /**
     * 被评论的目标ID（如 work.id）
     */
    @TableField("target_id")
    private Long targetId;

    @TableField("content")
    private String content;

    @TableField("likes_count")
    private Integer likesCount;

    @TableField("is_deleted")
    private Boolean isDeleted;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
