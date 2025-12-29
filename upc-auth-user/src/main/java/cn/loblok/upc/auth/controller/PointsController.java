package cn.loblok.upc.auth.controller;

import cn.loblok.upc.auth.common.util.PageConverter;
import cn.loblok.upc.auth.dto.UserPointsResponse;
import cn.loblok.upc.auth.dto.chickin.LeaderboardResponseDTO;
import cn.loblok.upc.auth.dto.chickin.PointTransactionDTO;
import cn.loblok.upc.auth.service.UserPointsService;
import cn.loblok.upc.auth.service.chickin.impl.LeaderboardServiceImpl;
import cn.loblok.upc.common.annotation.CurrentUser;
import cn.loblok.upc.common.base.PageResult;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.enums.CommonStatusEnum;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import cn.loblok.upc.auth.service.impl.PointsLogsService;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
@Slf4j
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointsController {

    private final UserPointsService pointsService;


    private final PointsLogsService pointsLogsService;


    private final LeaderboardServiceImpl leaderboardService;


    /**
     * 查询用户积分流水
     *
     * @param userId 用户ID
     * @param page 页码，默认为1
     * @param size 页面大小，默认为20
     * @param bizType 业务类型（可选）
     * @return 积分流水列表
     */
    @GetMapping("/transactions")
    public Result<PageResult<PointTransactionDTO>> getUserTransactions(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @CurrentUser Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int size,
            @RequestParam(value = "bizType", required = false) String bizType) {

        //暂不启用多租户
        tenantId = "default";
        // TODO: 权限校验`，上线前必须补！
        IPage<PointTransactionDTO> pageResult = pointsLogsService.getUserTransactions(tenantId, userId, bizType, page, size);

        PageResult<PointTransactionDTO> response = PageConverter.toPageResult(pageResult);
        return Result.success(response);
    }
    /**
     * 获取用户积分信息
     */
    @GetMapping
    public Result<UserPointsResponse> getUserPoints(@CurrentUser Long userId) {

        log.info("查询用户积分信息，用户ID: {}", userId);
        try {
            if (userId == null) {
                return Result.error(CommonStatusEnum.USER_CANNOT_EMPTY.getCode(), CommonStatusEnum.USER_EMPTY_ERROR.getMessage());
            }

            // 调用Service获取积分信息
            UserPointsResponse pointsResponse = pointsService.getUserPoints(userId);

            log.info("用户积分查询成功: {}", pointsResponse);
            return Result.success(pointsResponse);

        } catch (Exception e) {
            log.error("查询用户积分失败", e);
            return Result.error(500, "查询用户积分失败");
        }
    }

    /**
     * 获取积分排行榜
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 排行榜信息
     */
    @GetMapping("/points/leaderboard")
    public Result<LeaderboardResponseDTO> getLeaderboard(@RequestHeader("X-Tenant-ID") String tenantId,
                                                         @RequestParam("userId") Long userId) {
        return leaderboardService.getLeaderboard(tenantId, userId);
    }


}
