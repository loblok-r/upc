package cn.loblok.upc.modules.mall.service.impl;

import cn.loblok.upc.modules.mall.dto.UserPointsResponse;
import cn.loblok.upc.modules.mall.entity.UserPoints;
import cn.loblok.upc.mapper.UserPointsMapper;
import cn.loblok.upc.modules.mall.service.UserPointsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPointsServiceImpl extends ServiceImpl<UserPointsMapper, UserPoints> implements UserPointsService {


    private final UserPointsMapper userPointsMapper;


    @Override
    public boolean addUserPoints(Long userId, int deltaPoints) {

        QueryWrapper<UserPoints> userPointsQueryWrapper = new QueryWrapper<>();
        userPointsQueryWrapper.eq("user_id", userId);
        UserPoints userPoints = userPointsMapper.selectOne(userPointsQueryWrapper);

        if (userPoints == null) {
            // 如果用户没有积分记录，初始化一个
            log.info("用户 {} 无积分记录，创建默认记录", userId);
            userPoints = createDefaultUserPoints(userId);
        }
        userPoints.setBalance(userPoints.getBalance() + deltaPoints);
        userPoints.setTotalEarned(userPoints.getTotalEarned() + deltaPoints);
        userPoints.setUpdatedAt(LocalDateTime.now());
        return userPointsMapper.update(userPoints, userPointsQueryWrapper) > 0;
    }

    @Override
    public boolean reduceUserPoints(Long userId, int deltaPoints) {

        QueryWrapper<UserPoints> userPointsQueryWrapper = new QueryWrapper<>();
        userPointsQueryWrapper.eq("user_id", userId);
        UserPoints userPoints = userPointsMapper.selectOne(userPointsQueryWrapper);
        if (userPoints == null) {
            // 如果用户没有积分记录，初始化一个
            log.info("用户 {} 无积分记录，创建默认记录", userId);
            userPoints = createDefaultUserPoints(userId);
        }
        if (userPoints.getBalance() < deltaPoints) {
            return false;
        }
        userPoints.setBalance(userPoints.getBalance() - deltaPoints);
        userPoints.setTotalSpent(userPoints.getTotalSpent() + deltaPoints);
        userPoints.setUpdatedAt(LocalDateTime.now());
        return userPointsMapper.update(userPoints, userPointsQueryWrapper) > 0;
    }

    @Override
    public UserPointsResponse getUserPoints(Long userId) {


        QueryWrapper<UserPoints> userPointsQueryWrapper = new QueryWrapper<>();
        userPointsQueryWrapper.eq("user_id", userId);
        UserPoints userPoints = userPointsMapper.selectOne(userPointsQueryWrapper);

        if (userPoints == null) {
            log.info("用户 {} 无积分记录，创建默认记录", userId);
            userPoints = createDefaultUserPoints(userId);
        }

        Integer totalEarned = userPoints.getTotalEarned();
        Integer totalSpent = userPoints.getTotalSpent();

        return UserPointsResponse.builder()
                .balance(userPoints.getBalance())
                .totalEarned(totalEarned)
                .totalSpent(totalSpent)
                .build();
    }

    /**
     * 创建默认的用户积分记录
     */
    private UserPoints createDefaultUserPoints(Long userId) {
        UserPoints userPoints = new UserPoints();
        userPoints.setUserId(userId);
        userPoints.setBalance(0);
        userPoints.setCreatedAt(LocalDateTime.now());
        userPoints.setUpdatedAt(LocalDateTime.now());

        // 保存到数据库
        userPointsMapper.insert(userPoints);

        return userPoints;
    }

}
