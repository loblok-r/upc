package cn.loblok.upc.modules.checkin.dto;

import java.util.List;

/**
 * 签到历史响应体
 */
public class CheckinHistoryResponse {

    // yyyy-MM-dd
    private List<String> checkInHistory;

    public CheckinHistoryResponse(List<String> checkInHistory) {
        this.checkInHistory = checkInHistory;
    }

    public List<String> getCheckInHistory() {
        return checkInHistory;
    }
}