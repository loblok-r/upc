package cn.loblok.upc.controller;

import cn.loblok.upc.annotation.CurrentUser;
import cn.loblok.upc.dto.CreatePostRequest;
import cn.loblok.upc.dto.PostResponse;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.dto.TComment;
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
            return postsService.createPost(userId, createPostRequest);

        } catch (Exception e) {
            return Result.error(500, "发布失败", e.getMessage());
        }
    }

    /**
     * 获取我的帖子
     *
     * @param userId 当前用户ID
     * @return 我的帖子列表
     */
    @GetMapping("/mine")
    public  Result<List<PostResponse>>  getMyPosts(@CurrentUser Long userId) {
        List<PostResponse> posts = postsService.getMyPosts(userId);
        return Result.success(posts);
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

    /**
     * 获取帖子评论
     *
     * @param postId 帖子ID
     * @return 评论列表
     */
    @GetMapping("/{postId}/comments")
    public Result<List<TComment>> getPostComments(@PathVariable("postId") Long postId,
    @CurrentUser Long userId) {
        List<TComment> comments = postsService.getPostComments(postId, userId);
        return Result.success(comments);
    }

    /**
     * 点赞/取消点赞帖子
     *
     * @param postId 帖子ID
     * @param isLiked 是否点赞
     * @param userId 当前用户ID
     * @return 操作结果
     */
@PostMapping("/{postId}/{isLiked}")
    public Result<String> likePost(@PathVariable("postId") Long postId,
                                   @PathVariable("isLiked") String isLiked,
                                   @CurrentUser Long userId) {
        Boolean isLikedBool = "like".equals(isLiked);
        try {
            postsService.likePost(postId, isLikedBool, userId);
            if (isLikedBool) {
                return Result.success("点赞成功");
            } else {
                return Result.success("取消点赞成功");
            }
        } catch (Exception e) {
            if (isLikedBool) {
                return Result.error(500, "点赞失败", e.getMessage());
            } else {
                return Result.error(500, "取消点赞失败", e.getMessage());
            }
        }

    }

}