package cn.loblok.upc.dto;


import lombok.Data;

/**
 * AI绘图请求参数
 */
@Data
public class AiGenerateRequest {
    private String mode;          // "TEXT_CHAT", "AI_DRAWING"
    private String prompt;        // 用户输入
    private String referenceImage; // Base64 图像（可选，当前先忽略）
}