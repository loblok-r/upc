package cn.loblok.upc.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyUsageDTO {
    @Schema(description = "日使用次数")
    private int textChat;
    @Schema(description = "日使用次数")
    private int aiDrawing;
    @Schema(description = "日使用次数")
    private String lastResetDate; // 格式: "yyyy-MM-dd"
}