package cn.loblok.upc.dto;

import lombok.Data;

/**
 * 每日使用次数响应对象
 */
@Data
public class DailyUsageResponse {

    private int textChatCounts;
    private int aiDrawingCounts;
}