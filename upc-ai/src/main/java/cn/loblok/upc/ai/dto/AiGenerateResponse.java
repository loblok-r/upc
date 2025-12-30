package cn.loblok.upc.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "响应类型")
    private String type;
    /**
     *
     */
    @Schema(description = "响应内容")
    private String content;

    /**
     * 响应内容,图片模式临时路径
     */
    @Schema(description = "响应内容")
    private String imageUrl;
    /**
     * 响应内容,图片的真实路径
     */
@Schema(description = "响应内容")
    private String cosPath;

    /*
     * 会话ID
     */
    @Schema(description = "会话ID")
    private String sessionId;
    @Schema(description = "响应内容")
    private int width;
    @Schema(description = "响应内容")
    private int height;

}