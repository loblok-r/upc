package cn.loblok.upc.auth.dto.chickin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 签到历史响应体
 */
public class CheckinHistoryResponse {

    // yyyy-MM-dd
    @Schema(description = "签到历史")
    private List<String> checkInHistory;

    public CheckinHistoryResponse(List<String> checkInHistory) {
        this.checkInHistory = checkInHistory;
    }

    public List<String> getCheckInHistory() {
        return checkInHistory;
    }
}