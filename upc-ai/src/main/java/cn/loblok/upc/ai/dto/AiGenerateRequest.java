package cn.loblok.upc.ai.dto;


import cn.loblok.upc.common.enums.AppMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI绘图请求参数
 */
@Data
public class AiGenerateRequest {

    @NotBlank(message = "模式不能为空")
    @Schema(description = "模式")
    private String mode;          // "TEXT_CHAT", "AI_DRAWING"

    @NotBlank(message = "用户输入不能为空")
    @Schema(description = "用户输入")
    private String prompt;        // 用户输入,必填

    @Schema(description = "参考图像")
    private String referenceImage; // Base64 图像（可选，当前先忽略）

    @Schema(description = "session")
    private String sessionId;

    @Schema(description = "图像宽度", defaultValue = "512")
    private int width;

    @Schema(description = "图像高度", defaultValue = "512")
    private int height;
    public String getSize(){
        if(AppMode.AI_DRAWING.getMode().equals(mode)){
            return width + "x" + height;
        }else{
            return null;
        }
    }
}