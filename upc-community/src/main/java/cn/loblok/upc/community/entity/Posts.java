package cn.loblok.upc.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "id")
    private Long id;

    /**
     * 作者ID
     */
    @Schema(description = "作者ID")
    @TableField("user_id")
    private Long userId;

    /**
     * 多租户隔离
     */
    @Schema(description = "多租户隔离")
    @TableField("tenant_id")
    private String tenantId;

    /**
     * 作品标题（可选）
     */
    @Schema(description = "作品标题（可选）")
    @TableField("title")
    private String title;

    /**
     * 作品内容
     */
    @Schema(description = "作品内容")
    @TableField("content")
    private String content;

    /**
     * 该作品被点赞数（冗余字段，用于性能）
     */
    @Schema(description = "该作品被点赞数（冗余字段，用于性能）")
    @TableField("likes_count")
    private Integer likesCount;

    /**
     * 评论数
     */
    @Schema(description = "评论数")
    @TableField("comments_count")
    private Integer commentsCount;

    /**
     * 图片地址
     */
    @Schema(description = "图片地址")
    @TableField("image_url")
    private String imageUrl;

    /**
     * 是否删除
     */
    @Schema(description = "是否删除")
    @TableField("is_deleted")
    private Boolean isDeleted;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 图片大小
     */
    @Schema(description = "图片大小")
    @TableField("size")
    private String size;

    /**
     * 图片宽度
     */
    @Schema(description = "图片宽度")
    @TableField("width")
    private int width;

    /**
     * 图片高度
     */
    @Schema(description = "图片高度")
    @TableField("height")
    private int height;

}
