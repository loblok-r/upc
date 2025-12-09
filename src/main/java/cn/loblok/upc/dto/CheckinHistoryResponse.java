package cn.loblok.upc.dto;

import java.util.List;

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