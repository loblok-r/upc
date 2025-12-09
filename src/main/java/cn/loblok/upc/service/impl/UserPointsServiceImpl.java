package cn.loblok.upc.service.impl;

import cn.loblok.upc.dto.UserPointsResponse;
import cn.loblok.upc.entity.UserPoints;
import cn.loblok.upc.mapper.UserPointsMapper;
import cn.loblok.upc.service.UserPointsService;
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
    public UserPointsResponse getUserPoints(Long userId) {

        // 1. 查询用户积分记录
        QueryWrapper<UserPoints> userPointsQueryWrapper = new QueryWrapper<>();
        userPointsQueryWrapper.eq("user_id", userId);
        UserPoints userPoints = userPointsMapper.selectOne(userPointsQueryWrapper);

        if (userPoints == null) {
            // 如果用户没有积分记录，初始化一个
            log.info("用户 {} 无积分记录，创建默认记录", userId);
            userPoints = createDefaultUserPoints(userId);
        }

        // 2. 计算累计获得积分（需要查询积分流水表）
        Integer totalEarned = userPoints.getTotalEarned();

        // 3. 计算累计消费积分
        Integer totalSpent = userPoints.getTotalSpent();

        // 4. 构建响应
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
