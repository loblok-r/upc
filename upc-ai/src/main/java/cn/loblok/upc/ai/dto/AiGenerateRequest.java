package cn.loblok.upc.ai.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI绘图请求参数
 */
@Data
public class AiGenerateRequest {

    @NotBlank(message = "模式不能为空")
    private String mode;          // "TEXT_CHAT", "AI_DRAWING"

    @NotBlank(message = "用户输入不能为空")
    private String prompt;        // 用户输入,必填

    private String referenceImage; // Base64 图像（可选，当前先忽略）

    @NotBlank(message = "session不能为空")
    private String sessionId;

    private int width;

    private int height;
    public String getSize(){
        if("AI_DRAWING".equals(mode)){
            return width + "x" + height;
        }else{
            return null;
        }
    }
}