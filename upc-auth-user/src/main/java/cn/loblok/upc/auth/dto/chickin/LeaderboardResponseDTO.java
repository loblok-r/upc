package cn.loblok.upc.auth.dto.chickin;

import lombok.Data;
import java.util.List;

/**
 * 积分排行榜响应体
 */
@Data
public class LeaderboardResponseDTO {

    private List<LeaderboardItem> topList;
    private Long myRank;
    private Double myScore;

    @lombok.Data
    public static class LeaderboardItem {
        private String userId;
        private Double score;
        private Long rank;
    }

    public LeaderboardResponseDTO(List<LeaderboardItem> topList, Long myRank, Double myScore) {
        this.topList = topList;
        this.myRank = myRank;
        this.myScore = myScore;
    }
}