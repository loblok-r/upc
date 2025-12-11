package cn.loblok.upc.service;

import cn.loblok.upc.dto.CreatorLeaderboardResponse;

import java.util.List;

public interface CommunityService {

    /**
     * 获取创作者排行榜
     * @return 创作者排行榜
     */
    List<CreatorLeaderboardResponse> getCreatorLeaderboard(Long userId);

    /**
     * 获取新创作者排行榜
     * @return 新创作者排行榜
     */
    List<CreatorLeaderboardResponse> getNewCreatorLeaderboard(Long userId);
}
