package cn.loblok.upc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI绘图响应参数
 */
@Data
@AllArgsConstructor
public class AiGenerateResponse {

    /**
     * 响应类型 image  text
     */
    private String type;
    /**
     *
     */
    private String content;
    /**
     * 响应内容,图片模式有值
     */
    private String imageUrl;

    /*
     * 会话ID
     */
    private String sessionId;

}