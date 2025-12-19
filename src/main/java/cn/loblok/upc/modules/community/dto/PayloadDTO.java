package cn.loblok.upc.modules.community.dto;

import lombok.Data;

@Data
public class PayloadDTO {
    // 评论内容
    private String content;

    // 父级评论ID
    private Long parentId;
}