package cn.loblok.upc.community.entity;

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
 * 用户关注关系表
 * </p>
 *
 * @author loblok
 * @since 2025-12-11
 */
@Getter
@Setter
@TableName("follow")
public class Follow implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 粉丝（关注者）ID
     */
    @TableField("follower_id")
    private Long followerId;

    /**
     * 被关注者ID
     */
    @TableField("followee_id")
    private Long followeeId;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
