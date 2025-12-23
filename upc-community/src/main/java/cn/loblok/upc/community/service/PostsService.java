package cn.loblok.upc.community.service;

import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.enums.PostsTab;
import cn.loblok.upc.community.dto.CreatePostRequest;
import cn.loblok.upc.community.dto.PayloadDTO;
import cn.loblok.upc.community.dto.PostResponse;
import cn.loblok.upc.community.dto.TComment;
import cn.loblok.upc.community.entity.Posts;
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
     * 获取帖子列表
     *
     * @param userId 用户ID
     * @return 推荐帖子列表
     */
    List<PostResponse> getPostsTab(Long userId, int page, int pageSize, PostsTab postsTab);


    /**
     * 发布帖子
     *
     * @param userId 用户ID
     * @param createPostRequest 创建帖子请求
     * @return 帖子
     */
    Result<Posts> createPost(Long userId, CreatePostRequest createPostRequest);


    /**
     * 删除帖子
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 删除结果
     */
    void deletePost(Long postId, Long userId);
}