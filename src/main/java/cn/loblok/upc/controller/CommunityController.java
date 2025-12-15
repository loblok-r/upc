package cn.loblok.upc.controller;


import cn.loblok.upc.annotation.CurrentUser;
import cn.loblok.upc.dto.*;
import cn.loblok.upc.service.CommunityService;
import cn.loblok.upc.service.FollowService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community")
@AllArgsConstructor
public class CommunityController {


    private final FollowService followService;

    private final CommunityService communityService;



    /**
     * 关注或取消关注用户
     * 如果已经关注则取消关注，如果未关注则进行关注
     *
     * @param userId 当前用户ID
     * @param request 包含目标用户ID的请求体
     * @return 操作结果
     */
    @PostMapping("/users/follow")
    public Result<FollowUserResponse> followOrUnfollow(@CurrentUser Long userId, @RequestBody FollowUserRequest request) {
        try {
            FollowUserResponse response = followService.followOrUnfollow(userId, request.getUserId());
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(500, "操作失败", e.getMessage());
        }
    }
    
    /**
     * 查询当前用户关注的用户列表
     *
     * @param userId 当前用户ID
     * @return 关注的用户列表
     */
    @GetMapping("/following")
    public Result<List<Author>> getFollowingList(@CurrentUser Long userId) {
        try {
            List<Author> followingUsers = followService.getFollowingList(userId);
            return Result.success(followingUsers);
        } catch (Exception e) {
            return Result.error(500, "查询失败", e.getMessage());
        }
    }

    /**
     *
     * 查询当前推荐的推荐关注的用户列表
     *
     *
     */
    @GetMapping("/users/recommend")
    public Result<List<Author>> getRecommendFollowList(@CurrentUser Long userId) {
        try {
            List<Author> recommendUsers = followService.getRecommendFollowList(userId);
            return Result.success(recommendUsers);
        } catch (Exception e) {
            return Result.error(500, "查询失败", e.getMessage());
        }
    }

    /**
     *
     * 查询创作者排行榜
     *
     */
    @GetMapping("/leaderboard/creators")
    public Result<List<CreatorLeaderboardResponse>> getCreatorLeaderboard(@CurrentUser Long userId) {
        try {
            List<CreatorLeaderboardResponse> creatorLeaderboard = communityService.getCreatorLeaderboard(userId);
            return Result.success(creatorLeaderboard);
        } catch (Exception e) {
            return Result.error(500, "查询失败", e.getMessage());
        }
    }


    /**
     *
     * 查询新创作者排行榜
     *
     */
    @GetMapping("/leaderboard/newcreators")
    public Result<List<CreatorLeaderboardResponse>> getNewCreatorLeaderboard(@CurrentUser Long userId) {
        try {
            List<CreatorLeaderboardResponse> creatorLeaderboard = communityService.getNewCreatorLeaderboard(userId);
            return Result.success(creatorLeaderboard);
        } catch (Exception e) {
            return Result.error(500, "查询失败", e.getMessage());
        }
    }

    /**
     * 点赞或取消点赞评论
     * 如果已经点赞则取消点赞，如果未点赞则进行点赞
     *
     * @param userId 当前用户ID
     * @param commentId 评论ID
     * @return 操作结果
     */
    @PostMapping("/comments/{commentId}/like")
    public Result<String> likeOrUnlikeComment(@CurrentUser Long userId, @PathVariable Long commentId) {
        try {
            String response = communityService.likeOrUnlikeComment(userId, commentId);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(500, "操作失败", e.getMessage());
        }
    }

    /**
     * 获取用户个人资料
     *
     * @param targetUserId 目标用户ID
     * @param currentUserId 当前用户ID
     * @return 用户个人资料
     */
    @GetMapping("/users/{targetUserId}/profile")
    public Result<Author> getUserProfile(@PathVariable("targetUserId") Long targetUserId,
                                               @CurrentUser Long currentUserId) {
        try {
            Author authorInfo = communityService.getUserProfile(targetUserId, currentUserId);
            return Result.success(authorInfo);
        } catch (Exception e) {
            return Result.error(500, "获取用户资料失败", e.getMessage());
        }
    }

/**
     * 获取用户的作品列表
     *
     * @param targetUserId 目标用户ID
     * @param currentUserId 当前用户ID
     * @return 用户作品列表
     */
    @GetMapping("/users/{targetUserId}/works")
    public Result<List<PostResponse>> getUserWorks(@PathVariable("targetUserId") Long targetUserId,
                                           @CurrentUser Long currentUserId) {
        try {
            return communityService.getUserWorks(targetUserId, currentUserId);
        } catch (Exception e) {
            return Result.error(500, "获取用户作品失败", e.getMessage());
        }
    }


}