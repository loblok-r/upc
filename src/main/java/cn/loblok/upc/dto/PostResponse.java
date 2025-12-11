package cn.loblok.upc.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子响应体
 */
@Data
public class PostResponse {


    private Author author;

    private String title;

    private String content;

    private Integer likesCount;

    private Integer commentsCount;

    private String imageUrl;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}



