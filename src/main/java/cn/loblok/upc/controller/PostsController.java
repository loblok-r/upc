package cn.loblok.upc.controller;

import cn.loblok.upc.annotation.CurrentUser;
import cn.loblok.upc.dto.CreatePostRequest;
import cn.loblok.upc.dto.PostResponse;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.entity.Posts;
import cn.loblok.upc.service.PostsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 用户作品/帖子表 前端控制器
 * </p>
 *
 * @author loblok
 * @since 2025-12-11
 */
@RestController
@RequestMapping("/api/community/posts")
public class PostsController {

    @Autowired
    private PostsService postsService;

    /**
     * 发布帖子
     *
     * @param userId 当前用户ID
     * @return 发布结果
     */
    @PostMapping("/create")
    public Result<Posts> createPost(@CurrentUser Long userId, @RequestBody CreatePostRequest createPostRequest) {
        try {

            Posts posts = new Posts();
            // 设置帖子的基本信息
            posts.setUserId(userId);
            posts.setTitle(createPostRequest.getTitle());
            posts.setContent(createPostRequest.getContent());
            posts.setImageUrl(createPostRequest.getImageUrl());
            posts.setCreatedAt(LocalDateTime.now());
            posts.setUpdatedAt(LocalDateTime.now());
            posts.setLikesCount(0);
            posts.setCommentsCount(0);
            posts.setIsDeleted(false);
            
            // 保存帖子
            boolean saved = postsService.save(posts);
            
            if (saved) {
                return Result.success(posts);
            } else {
                return Result.error(500, "发布失败", "帖子保存失败");
            }
        } catch (Exception e) {
            return Result.error(500, "发布失败", e.getMessage());
        }
    }

    /**
     * 获取推荐帖子
     *
     * @param userId 当前用户ID
     * @return 推荐帖子列表
     */
    @GetMapping("/recommend")
    public Result<List<PostResponse>> getRecommendPosts(@CurrentUser Long userId) {
        List<PostResponse> posts = postsService.getRecommendPosts(userId);
        return Result.success(posts);
    }

    /**
     * 获取关注用户的帖子
     *
     * @param userId 当前用户ID
     * @return 关注用户的帖子列表
     */
    @GetMapping("/following")
    public Result<List<PostResponse>> getFollowingPosts(@CurrentUser Long userId) {
        List<PostResponse> posts = postsService.getFollowingPosts(userId);
        return Result.success(posts);
    }

    /**
     * 获取最新帖子
     *
     * @param userId 当前用户ID
     * @return 最新帖子列表
     */
    @GetMapping("/latest")
    public Result<List<PostResponse>> getLatestPosts(@CurrentUser Long userId) {
        List<PostResponse> posts = postsService.getLatestPosts(userId);
        return Result.success(posts);
    }

}