package cn.loblok.upc.modules.community.controller;

import cn.loblok.upc.common.annotation.CurrentUser;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.modules.community.dto.CreatePostRequest;
import cn.loblok.upc.modules.community.dto.PayloadDTO;
import cn.loblok.upc.modules.community.dto.PostResponse;
import cn.loblok.upc.modules.community.dto.TComment;
import cn.loblok.upc.modules.community.entity.Posts;
import cn.loblok.upc.modules.community.service.PostsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Result<List<PostResponse>> getMyPosts(@CurrentUser Long userId) {
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
    public Result<List<PostResponse>> getRecommendPosts(@CurrentUser Long userId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int pageSize) {
        List<PostResponse> posts = postsService.getRecommendPosts(userId, page, pageSize);
        return Result.success(posts);
    }

    /**
     * 获取关注用户的帖子
     *
     * @param userId 当前用户ID
     * @return 关注用户的帖子列表
     */
    @GetMapping("/following")
    public Result<List<PostResponse>> getFollowingPosts(@CurrentUser Long userId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int pageSize) {
        List<PostResponse> posts = postsService.getFollowingPosts(userId, page, pageSize);
        return Result.success(posts);
    }

    /**
     * 获取最新帖子
     *
     * @param userId 当前用户ID
     * @return 最新帖子列表
     */
    @GetMapping("/latest")
    public Result<List<PostResponse>> getLatestPosts(@CurrentUser Long userId,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "20") int pageSize) {
        List<PostResponse> posts = postsService.getLatestPosts(userId, page, pageSize);
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
     * @param postId  帖子ID
     * @param isLiked 是否点赞
     * @param userId  当前用户ID
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

    /**
     * 添加评论
     *
     * @param postId     帖子ID
     * @param payloadDTO 评论内容
     * @param userId     当前用户ID
     * @return 添加结果
     */
    @PostMapping("/{postId}/comments")
    public Result<TComment> addComment(@PathVariable("postId") Long postId,
                                       @RequestBody PayloadDTO payloadDTO,
                                       @CurrentUser Long userId) {
        try {

            return postsService.addComment(postId, payloadDTO, userId);
        } catch (Exception e) {
            return Result.error(500, "添加评论失败", e.getMessage());
        }
    }

/**
     * 删除帖子
     *
     * @param postId 帖子ID
     * @param userId 当前用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{postId}")
    public Result<String> deletePost(@PathVariable("postId") Long postId, @CurrentUser Long userId) {
        try {
            postsService.deletePost(postId, userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(500, "删除失败", e.getMessage());
        }
    }

}