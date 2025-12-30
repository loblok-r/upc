package cn.loblok.upc.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AiResult {
    @Schema(description = "内容")
    private String content;
    @Schema(description = "图片地址")
    private String imageUrl; //展示内容
    @Schema(description = "cos path")
    private String cosPath; //cos path
}