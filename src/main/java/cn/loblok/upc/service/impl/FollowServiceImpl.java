package cn.loblok.upc.service.impl;

import cn.loblok.upc.dto.FollowUserResponse;
import cn.loblok.upc.entity.Follow;
import cn.loblok.upc.entity.User;
import cn.loblok.upc.mapper.FollowMapper;
import cn.loblok.upc.service.FollowService;
import cn.loblok.upc.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Autowired
    private UserService userService;

    @Override
    public FollowUserResponse followOrUnfollow(Long followerId, Long followeeId) {
        // 检查是否已经关注
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("follower_id", followerId);
        queryWrapper.eq("followee_id", followeeId);
        
        Follow existingFollow = this.getOne(queryWrapper);
        FollowUserResponse response = new FollowUserResponse();
        
        if (existingFollow != null) {
            // 已经关注，执行取消关注操作
            this.remove(queryWrapper);
            // 设置操作结果描述
            response.setFollowerCount(getFollowerCount(followeeId));
            return response;
        } else {
            // 未关注，执行关注操作
            Follow follow = new Follow();
            follow.setFollowerId(followerId);
            follow.setFolloweeId(followeeId);
            follow.setCreatedAt(java.time.LocalDateTime.now());
            // 注意：tenant_id 字段需要根据实际业务逻辑设置
            this.save(follow);
            response.setFollowerCount(getFollowerCount(followeeId));
            return response;
        }
    }
    
    /**
     * 获取被关注者的粉丝数量
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
     * @param userId 用户ID
     * @return 关注的用户列表
     */
    @Override
    public List<User> getFollowingList(Long userId) {
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
        
        return userService.listByIds(followingIds);
    }
}