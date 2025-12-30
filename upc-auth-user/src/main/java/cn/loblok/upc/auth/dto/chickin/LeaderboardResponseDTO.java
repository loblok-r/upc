package cn.loblok.upc.auth.dto.chickin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * 积分排行榜响应体
 */
@Data
public class LeaderboardResponseDTO {

    /**
     * 排行榜列表
     */
    @Schema(description = "排行榜列表")
    private List<LeaderboardItem> topList;
    /**
     * 我的排名
     */
    @Schema(description = "我的排名")
    private Long myRank;
    /**
     * 我的积分
     */
    @Schema(description = "我的积分")
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