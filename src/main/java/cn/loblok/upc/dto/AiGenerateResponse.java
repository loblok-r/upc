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
     * 响应内容,文本模式有值
     */
    private String content;
    /**
     * 响应内容,图片模式有值
     */
    private String imageUrl;

}