package cn.loblok.upc.dto;

import lombok.Data;

/**
 * 发布作品请求
 */
@Data
public class CreateWorkRequest {
    private String title;
    private String content;
    // getters and setters
}