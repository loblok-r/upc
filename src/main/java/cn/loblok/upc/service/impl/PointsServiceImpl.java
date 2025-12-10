package cn.loblok.upc.service.impl;

import cn.loblok.upc.entity.User;
import cn.loblok.upc.service.PointsService;
import cn.loblok.upc.service.UserPointsService;
import cn.loblok.upc.service.UserService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class PointsServiceImpl implements PointsService {

    private final UserPointsService userPointsService;

    private final UserService userService;


    @Override
    public boolean updateUserPoints(Long userId, int deltaPoints) {
        return false;
    }

    @Override
    public boolean reduceUserPoints(Long userId, int deltaPoints) {
        log.info("用户 {} 减少积分 {}", userId, deltaPoints);

        User user = userService.getById(userId);
        if (user == null) {
            log.warn("用户 {} 不存在", userId);
            return false;
        }
        
        if (userPointsService.reduceUserPoints(userId, deltaPoints)) {
            user.setPoints(user.getPoints() - deltaPoints);
            userService.update(user, new UpdateWrapper<User>().eq("id", userId));
            return true;
        }
        return false;
    }

    @Override
    public boolean addUserPoints(Long userId, int deltaPoints) {
        log.info("用户 {} 增加积分 {}", userId, deltaPoints);

        User user = userService.getById(userId);
        if (user == null) {
            log.warn("用户 {} 不存在", userId);
            return false;
        }

        if (userPointsService.addUserPoints(userId, deltaPoints)) {
            user.setPoints(user.getPoints() + deltaPoints);
            userService.update(user, new UpdateWrapper<User>().eq("id", userId));
            return true;
        }
        return false;
    }
}