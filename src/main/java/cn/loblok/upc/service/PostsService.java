package cn.loblok.upc.service;

import cn.loblok.upc.dto.CreatePostRequest;
import cn.loblok.upc.dto.PostResponse;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.dto.TComment;
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
    List<PostResponse> getRecommendPosts(Long userId);

    /**
     * 获取关注用户的帖子
     *
     * @param userId 用户ID
     * @return 关注用户的帖子列表
     */
    List<PostResponse> getFollowingPosts(Long userId);

    /**
     * 获取最新帖子
     *
     * @param userId 用户ID
     * @return 最新帖子列表
     */
    List<PostResponse> getLatestPosts(Long userId);

    /**
     * 获取我的帖子
     *
     * @param userId 用户ID
     * @return 我的帖子列表
     */
    List<PostResponse> getMyPosts(Long userId);

    /**
     * 发布帖子
     *
     * @param userId 用户ID
     * @param createPostRequest 创建帖子请求
     * @return 帖子
     */
    Result<Posts> createPost(Long userId, CreatePostRequest createPostRequest);

    /**
     * 获取帖子评论
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 帖子评论列表
     */
    List<TComment> getPostComments(Long postId, Long userId);

    void likePost(Long postId, Boolean isLikedBool, Long userId);
}