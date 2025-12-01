package cn.loblok.upc.controller;

import cn.loblok.upc.dto.LeaderboardResponseDTO;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.service.impl.LeaderboardServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LeaderboardController {
    
    @Autowired
    private LeaderboardServiceImpl leaderboardService;
    
    /**
     * 获取积分排行榜
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @return 排行榜信息
     */
    @GetMapping("/leaderboard")
    public Result<LeaderboardResponseDTO> getLeaderboard(@RequestHeader("X-Tenant-ID") String tenantId,
                                                         @RequestParam("userId") Long userId) {
        return leaderboardService.getLeaderboard(tenantId, userId);
    }
}