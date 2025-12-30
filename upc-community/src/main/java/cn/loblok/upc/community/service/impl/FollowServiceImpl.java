package cn.loblok.upc.community.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.loblok.upc.api.user.dto.UserPublicInfoDTO;
import cn.loblok.upc.api.user.feign.UserFeignClient;
import cn.loblok.upc.api.worker.dto.StatUpdateMsgDTO;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.community.dto.FollowUserResponse;
import cn.loblok.upc.community.entity.Follow;
import cn.loblok.upc.community.mapper.FollowMapper;
import cn.loblok.upc.community.service.FollowService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户关注关系表 服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-11
 */
@Service
@Slf4j
@AllArgsConstructor
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    private final UserFeignClient userFeignClient;

    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public FollowUserResponse followOrUnfollow(Long followerId, Long followeeId) {

        log.info("开始关注/取关用户 {}", followerId);

        if (followerId.equals(followeeId)) {
            log.info("不能关注自己");
            return null;
        }

        // 检查是否已经关注
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("follower_id", followerId);
        queryWrapper.eq("followee_id", followeeId);

        Follow existingFollow = this.getOne(queryWrapper);
        FollowUserResponse response = new FollowUserResponse();

        if (existingFollow != null) {
            log.info("已关注, 取关");
            // 已经关注，执行取消关注操作
            this.remove(queryWrapper);

            String bizId = IdUtil.randomUUID();
            CorrelationData correlationData = new CorrelationData(bizId);

            // 更新被关注者的粉丝数，粉丝数减一 & 更新当前用户的关注数，关注减一
            StatUpdateMsgDTO msg = new StatUpdateMsgDTO();
            msg.setUserId(followerId);
            msg.setTargetUserId(followeeId);
            msg.setDelta(-1);
            msg.setType("FOLLOW");
            rabbitTemplate.convertAndSend(
                    "upc.direct.exchange",
                    "mq.route.stats_update",
                    msg,
                    message -> {
                        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return message;
                    },
                    correlationData
            );


            // 设置操作结果描述
            response.setFollowerCount(getFollowerCount(followeeId));
            return response;
        } else {
            log.info("{}未关注{}, 关注", followerId, followeeId);
            // 未关注，执行关注操作
            Follow follow = new Follow();
            follow.setFollowerId(followerId);
            follow.setFolloweeId(followeeId);
            follow.setCreatedAt(java.time.LocalDateTime.now());
//            follow.setTenantId("default");
            this.save(follow);
            // 更新被关注者的粉丝数
            String bizId = IdUtil.randomUUID();
            CorrelationData correlationData = new CorrelationData(bizId);

            StatUpdateMsgDTO msg = new StatUpdateMsgDTO();
            msg.setUserId(followerId);
            msg.setTargetUserId(followeeId);
            msg.setDelta(1);
            msg.setType("FOLLOW");
            rabbitTemplate.convertAndSend(
                    "upc.direct.exchange",
                    "mq.route.stats_update",
                    msg,
                    message -> {
                        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return message;
                    },
                    correlationData
            );

            response.setFollowerCount(getFollowerCount(followeeId));
            return response;
        }
    }

    /**
     * 获取被关注者的粉丝数量
     *
     * @param followeeId 被关注者ID
     * @return 粉丝数量
     */
    private Integer getFollowerCount(Long followeeId) {
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("followee_id", followeeId);
        return Math.toIntExact(this.count(queryWrapper));
    }

    /**
     * 获取用户关注的用户列表
     *
     * @param userId 用户ID
     * @return 关注的用户列表
     */
    @Override
    public List<UserPublicInfoDTO> getFollowingList(Long userId) {
        List<Long> followingIds = findFollowedIds(userId);

        // 根据ID列表查询用户信息
        if (followingIds.isEmpty()) {
            return List.of();
        }

        Result<Map<Long, UserPublicInfoDTO>> userPublicInfoBatch = userFeignClient.getUserPublicInfoBatch(followingIds);

        return userPublicInfoBatch.getData().values().stream().collect(Collectors.toList());
    }



    @Override
    public List<UserPublicInfoDTO> getFollowerList(Long userId) {
        List<Long> followerIds = findFlowerIds(userId);

        // 根据ID列表查询用户信息
        if (followerIds.isEmpty()) {
            return List.of();
        }

        Result<Map<Long, UserPublicInfoDTO>> userPublicInfoBatch = userFeignClient.getUserPublicInfoBatch(followerIds);

        return userPublicInfoBatch.getData().values().stream().collect(Collectors.toList());
    }

    @Override
    public  List<Long> findFlowerIds(Long userId) {
        // 查询关注指定用户的所有粉丝ID
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("followee_id", userId);

        List<Follow> followerRelations = this.list(queryWrapper);

        // 提取粉丝的ID列表
        List<Long> followerIds = followerRelations.stream()
                .map(Follow::getFollowerId)
                .collect(Collectors.toList());
        return followerIds;
    }


    @Override
    public List<UserPublicInfoDTO> getRecommendFollowList(Long userId) {
        log.info("开始获取 推荐的用户 {}", userId);

        // 获取当前用户关注的用户ID
        List<Long> followedIds = this.lambdaQuery().eq(Follow::getFollowerId, userId).list().stream().map(follow -> follow.getFolloweeId()).collect(Collectors.toList());


        followedIds.add(userId);
        Result<List<UserPublicInfoDTO>> result = userFeignClient.getRecommendedUsers(10, followedIds);

        return result.getData();
    }


    @Override
    public boolean isFollowed(Long userId, Long targetId) {
        // 检查用户是否关注了目标用户
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("follower_id", userId);
        queryWrapper.eq("followee_id", targetId);

        return this.count(queryWrapper) > 0;
    }

    @Override
    public List<Long> findFollowedIds(Long currentUserId, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        // 查询当前用户关注的用户ID列表
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("follower_id", currentUserId);
        queryWrapper.in("followee_id", userIds);

        return this.list(queryWrapper).stream()
                .map(Follow::getFolloweeId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> findFollowedIds(Long userId) {
        // 查询用户关注的所有用户ID
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("follower_id", userId);

        List<Follow> followingRelations = this.list(queryWrapper);

        // 提取被关注者的ID列表
        List<Long> followingIds = followingRelations.stream()
                .map(Follow::getFolloweeId)
                .collect(Collectors.toList());
        return followingIds;
    }

}