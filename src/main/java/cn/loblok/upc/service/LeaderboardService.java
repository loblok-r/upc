package cn.loblok.upc.service;

import cn.loblok.upc.dto.LeaderboardResponseDTO;
import cn.loblok.upc.dto.Result;

public interface LeaderboardService {
    
    /**
     * 获取排行榜信息
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @return 排行榜信息
     */
    Result<LeaderboardResponseDTO> getLeaderboard(String tenantId, Long userId);
}