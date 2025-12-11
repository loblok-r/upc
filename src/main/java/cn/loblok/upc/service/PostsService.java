package cn.loblok.upc.service;

import cn.loblok.upc.entity.Posts;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户作品/帖子表 服务类
 * </p>
 *
 * @author loblok
 * @since 2025-12-11
 */
public interface PostsService extends IService<Posts> {

    /**
     * 获取推荐帖子
     *
     * @param userId 用户ID
     * @return 推荐帖子列表
     */
    List<Posts> getRecommendPosts(Long userId);

    /**
     * 获取关注用户的帖子
     *
     * @param userId 用户ID
     * @return 关注用户的帖子列表
     */
    List<Posts> getFollowingPosts(Long userId);

    /**
     * 获取最新帖子
     *
     * @param userId 用户ID
     * @return 最新帖子列表
     */
    List<Posts> getLatestPosts(Long userId);
}