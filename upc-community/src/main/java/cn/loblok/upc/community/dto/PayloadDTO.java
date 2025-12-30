package cn.loblok.upc.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PayloadDTO {
    // 评论内容
    @Schema(description = "评论内容")
    private String content;

    // 父级评论ID
    @Schema(description = "父级评论ID")
    private Long parentId;
}