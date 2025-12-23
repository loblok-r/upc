package cn.loblok.upc.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyUsageDTO {
    private int textChat;
    private int aiDrawing;
    private String lastResetDate; // 格式: "yyyy-MM-dd"
}