package cn.loblok.upc.dto;

import lombok.Data;

@Data
public class PayloadDTO {
    // 评论内容
    private String content;

    // 父级评论ID
    private Long parentId;
}