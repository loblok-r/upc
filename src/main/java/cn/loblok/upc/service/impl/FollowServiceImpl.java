package cn.loblok.upc.service.impl;

import cn.loblok.upc.dto.Author;
import cn.loblok.upc.dto.FollowUserResponse;
import cn.loblok.upc.entity.Follow;
import cn.loblok.upc.entity.User;
import cn.loblok.upc.mapper.FollowMapper;
import cn.loblok.upc.service.FollowService;
import cn.loblok.upc.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


    private final UserService userService;

    @Override
    @Transactional
    public FollowUserResponse followOrUnfollow(Long followerId, Long followeeId) {

        log.info("开始关注/取关用户 {}", followerId);

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
    public List<Author> getFollowingList(Long userId) {
        // 查询用户关注的所有用户ID
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("follower_id", userId);

        List<Follow> followingRelations = this.list(queryWrapper);

        // 提取被关注者的ID列表
        List<Long> followingIds = followingRelations.stream()
                .map(Follow::getFolloweeId)
                .collect(Collectors.toList());

        // 根据ID列表查询用户信息
        if (followingIds.isEmpty()) {
            return List.of();
        }

        List<User> users = userService.listByIds(followingIds);
        return users.stream()
                .map(user -> {
                    // 将User对象转换为Author对象
                    return convertToAuthor(user);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Author> getRecommendFollowList(Long userId) {
        log.info("开始获取 推荐的用户 {}", userId);
        // 获取所有用户
        List<User> allUsers = userService.list();

        // 获取当前用户关注的用户ID
        List<Long> hasFollweeIdlist = this.lambdaQuery().eq(Follow::getFollowerId, userId).list().stream().map(follow -> follow.getFolloweeId()).collect(Collectors.toList());


        log.info("当前用户关注了{}个用户", hasFollweeIdlist.size());
        // 根据粉丝数和获赞数进行排序，粉丝数权重0.6，获赞数权重0.4
        List<User> list = allUsers.stream()
                // 过滤掉当前用户关注的用户
                .filter(user -> !hasFollweeIdlist.contains(user.getId()))
                .sorted((user1, user2) -> {
                    long score2 = Math.round(user2.getFollowers() * 0.6 + user2.getLikes() * 0.4);
                    long score1 = Math.round(user1.getFollowers() * 0.6 + user1.getLikes() * 0.4);
                    return Long.compare(score2, score1);
                })
                .limit(10) // 限制返回前10个推荐用户
                .toList();

        return list.stream()
                .map(user -> {
                    // 将User对象转换为Author对象
                    return convertToAuthor(user);
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean isFollowed(Long userId, Long targetId) {
        // 检查用户是否关注了目标用户
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("follower_id", userId);
        queryWrapper.eq("followee_id", targetId);

        return this.count(queryWrapper) > 0;
    }


    /**
     * 将User对象转换为Author对象
     *
     * @param user User对象
     * @return Author对象
     */
    public Author convertToAuthor(User user) {
        Author author = new Author();
        author.setId(user.getId());
        author.setName(user.getUsername());
        author.setAvatar(user.getAvatarUrl());
        author.setHandle(user.getUsername());
        author.setFollowers(user.getFollowers());
        return author;
    }

}