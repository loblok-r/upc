package cn.loblok.upc.modules.checkin.service;

import cn.loblok.upc.modules.checkin.dto.LeaderboardResponseDTO;
import cn.loblok.upc.common.base.Result;

public interface LeaderboardService {
    
    /**
     * 获取排行榜信息
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @return 排行榜信息
     */
    Result<LeaderboardResponseDTO> getLeaderboard(String tenantId, Long userId);
}