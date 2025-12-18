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
     * 响应内容,图片模式临时路径
     */
    private String imageUrl;
    /**
     * 响应内容,图片的真实路径
     */

    private String cosPath;

    /*
     * 会话ID
     */
    private String sessionId;

    private int width;

    private int height;

}