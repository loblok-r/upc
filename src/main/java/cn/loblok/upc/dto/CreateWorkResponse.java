package cn.loblok.upc.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 发布作品响应
 */
@Data
public class CreateWorkResponse {
    private Long workId;
    private LocalDateTime createdAt;
    // getters and setters
}