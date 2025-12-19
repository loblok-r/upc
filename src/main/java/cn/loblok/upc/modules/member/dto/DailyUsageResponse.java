package cn.loblok.upc.modules.member.dto;

import lombok.Data;

/**
 * 每日使用次数响应对象
 */
@Data
public class DailyUsageResponse {

    private int textChatCounts;
    private int aiDrawingCounts;
}