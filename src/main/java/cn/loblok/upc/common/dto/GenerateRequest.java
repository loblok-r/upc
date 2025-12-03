package cn.loblok.upc.common.dto;

import lombok.Data;

import java.util.Map;

@Data
public class GenerateRequest {
    private String plan; // "BASIC", "HD", "PRO"
    private String imageUrl; // 用户上传的原始图 URL 或 Base64（Mock 可忽略）
    private String style;   // 可选，如 "anime", "cyberpunk"
    // Pro 才生效的字段（可选）
    private Map<String, Object> options;
}