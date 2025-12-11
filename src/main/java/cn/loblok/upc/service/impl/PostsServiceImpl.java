package cn.loblok.upc.service.impl;

import cn.loblok.upc.entity.Posts;
import cn.loblok.upc.mapper.PostsMapper;
import cn.loblok.upc.service.FollowService;
import cn.loblok.upc.service.PostsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户作品/帖子表 服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-11
 */
@Service
public class PostsServiceImpl extends ServiceImpl<PostsMapper, Posts> implements PostsService {

    @Autowired
    private FollowService followService;

    /**
     * 获取推荐帖子
     *
     * @param userId 用户ID
     * @return 推荐帖子列表
     */
    @Override
    public List<Posts> getRecommendPosts(Long userId) {
        // 简单实现：获取所有帖子按点赞数倒序排列
        QueryWrapper<Posts> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        queryWrapper.orderByDesc("likes_count");
        queryWrapper.last("LIMIT 20"); // 限制返回20条
        return this.list(queryWrapper);
    }

    /**
     * 获取关注用户的帖子
     *
     * @param userId 用户ID
     * @return 关注用户的帖子列表
     */
    @Override
    public List<Posts> getFollowingPosts(Long userId) {
        // 先获取当前用户关注的所有用户ID
        List<Long> followingIds = followService.getFollowingList(userId).stream()
                .map(user -> user.getId())
                .toList();

        if (followingIds.isEmpty()) {
            return List.of(); // 如果没有关注任何人，则返回空列表
        }

        // 查询关注用户发布的帖子
        QueryWrapper<Posts> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", followingIds);
        queryWrapper.eq("is_deleted", false);
        queryWrapper.orderByDesc("created_at");
        queryWrapper.last("LIMIT 20"); // 限制返回20条
        return this.list(queryWrapper);
    }

    /**
     * 获取最新帖子
     *
     * @param userId 用户ID
     * @return 最新帖子列表
     */
    @Override
    public List<Posts> getLatestPosts(Long userId) {
        QueryWrapper<Posts> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        queryWrapper.orderByDesc("created_at");
        queryWrapper.last("LIMIT 20"); // 限制返回20条
        return this.list(queryWrapper);
    }
}