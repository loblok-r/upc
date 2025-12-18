package cn.loblok.upc.entity;

import cn.loblok.upc.util.TencentCOSUtil;
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
 * 用户作品/帖子表
 * </p>
 *
 * @author loblok
 * @since 2025-12-11
 */
@Getter
@Setter
@TableName("posts")
public class Posts implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 作者ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 多租户隔离
     */
    @TableField("tenant_id")
    private String tenantId;

    /**
     * 作品标题（可选）
     */
    @TableField("title")
    private String title;

    /**
     * 作品内容
     */
    @TableField("content")
    private String content;

    /**
     * 该作品被点赞数（冗余字段，用于性能）
     */
    @TableField("likes_count")
    private Integer likesCount;

    /**
     * 评论数
     */
    @TableField("comments_count")
    private Integer commentsCount;

    @TableField("image_url")
    private String imageUrl;

    @TableField("is_deleted")
    private Boolean isDeleted;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("size")
    private String size;

    @TableField("width")
    private int width;

    @TableField("height")
    private int height;

}
