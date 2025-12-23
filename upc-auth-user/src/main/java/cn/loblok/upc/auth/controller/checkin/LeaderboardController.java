package cn.loblok.upc.auth.controller.checkin;

import cn.loblok.upc.auth.dto.chickin.LeaderboardResponseDTO;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.auth.service.chickin.impl.LeaderboardServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 积分排行榜控制器
 */
@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    @Autowired
    private LeaderboardServiceImpl leaderboardService;

    /**
     * 获取积分排行榜
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 排行榜信息
     */
    @GetMapping
    public Result<LeaderboardResponseDTO> getLeaderboard(@RequestHeader("X-Tenant-ID") String tenantId,
                                                         @RequestParam("userId") Long userId) {
        return leaderboardService.getLeaderboard(tenantId, userId);
    }
}