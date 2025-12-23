package cn.loblok.upc.auth.service.impl;

import cn.loblok.upc.auth.mapper.UserMapper;
import cn.loblok.upc.auth.entity.User;
import cn.loblok.upc.auth.service.PointsService;
import cn.loblok.upc.auth.service.UserPointsService;
import cn.loblok.upc.auth.service.UserService;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.enums.CommonStatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class PointsServiceImpl implements PointsService {

    private final UserMapper userMapper;

    private final UserService userService;

    private final UserPointsService userPointsService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> reduceUserPoints(Long userId, int deltaPoints) {
        log.info("用户 {} 减少积分 {}", userId, deltaPoints);

        if (userId == null || deltaPoints <= 0) {
            return Result.error(CommonStatusEnum.PARAM_ERROR);
        }

        int updated = userMapper.reducePoints(userId, deltaPoints);
        if (updated == 0) {
            // 可能是用户不存在，也可能是积分不足
            User user = userService.getById(userId);
            if (user == null) {
                log.warn("减少积分失败：用户 {} 不存在", userId);
                return Result.error(CommonStatusEnum.USER_NOT_FOUND);
            } else {
                log.warn("用户 {} 积分不足，当前积分: {}, 尝试扣除: {}", userId, user.getPoints(), deltaPoints);
                return Result.error(CommonStatusEnum.INSUFFICIENT_POINTS); // 建议新增这个状态码
            }
        }

        //更新积分表
        boolean result = userPointsService.reduceUserPoints(userId, deltaPoints);
        log.info("用户 {} 成功扣除积分 {}", userId, deltaPoints);
        return Result.success(null);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> addUserPoints(Long userId, int deltaPoints) {
        log.info("用户 {} 增加积分 {}", userId, deltaPoints);

        if (userId == null || deltaPoints <= 0) {
            return Result.error(CommonStatusEnum.PARAM_ERROR);
        }

        int updated = userMapper.addPoints(userId, deltaPoints);
        if (updated == 0) {
            log.warn("增加积分失败：用户 {} 不存在", userId);
            return Result.error(CommonStatusEnum.USER_NOT_FOUND);
        }

        //更新积分表
        boolean result = userPointsService.addUserPoints(userId, deltaPoints);

        log.info("用户 {} 成功增加积分 {}", userId, deltaPoints);
        return Result.success(null);

    }
}