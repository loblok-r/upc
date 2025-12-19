package cn.loblok.upc.modules.mall.controller;

import cn.loblok.upc.common.annotation.CurrentUser;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.modules.mall.dto.UserPointsResponse;
import cn.loblok.upc.common.enums.CommonStatusEnum;
import cn.loblok.upc.modules.mall.service.UserPointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/api/mall/user")
@RequiredArgsConstructor
public class UserPointsController {

    private final UserPointsService pointsService;

    /**
     * 获取用户积分信息
     */
    @GetMapping("/points")
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


}
