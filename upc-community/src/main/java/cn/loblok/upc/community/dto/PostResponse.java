package cn.loblok.upc.community.dto;

import cn.loblok.upc.api.user.dto.UserPublicInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子响应体
 */
@Data
public class PostResponse {

    /**
     * 帖子ID
     */
    @Schema(description = "帖子ID")
    private Long id;

    /**
     * 作者信息
     */
    @Schema(description = "作者信息")
    private CommunityUserVO author;

    /**
     * 标题
     */
    @Schema(description = "标题")
    private String title;

    /**
     * 内容
     */
    @Schema(description = "内容")
    private String content;

    /**
     * 点赞数
     */
    @Schema(description = "点赞数")
    private Integer likesCount;

    /**
     * 评论数
     */
    @Schema(description = "评论数")
    private Integer commentsCount;

    /**
     * 图片URL
     */
    @Schema(description = "图片URL")
    private String imageUrl;

    /**
     * 是否删除
     */
    @Schema(description = "是否删除")
    private Boolean isDeleted;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 图片宽度
     */
    @Schema(description = "图片宽度")
    private int width;

    /**
     * 图片高度
     */
    @Schema(description = "图片高度")
    private int height;
}



