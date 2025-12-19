package cn.loblok.upc.modules.community.controller;


import cn.loblok.upc.common.annotation.CurrentUser;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.modules.community.dto.*;
import cn.loblok.upc.modules.community.service.CommunityService;
import cn.loblok.upc.modules.community.service.FollowService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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
 * 查询用户关注列表或粉丝列表
 *
 * @param userId 用户ID
 * @param type 类型: "following"表示关注列表, "followers"表示粉丝列表
 * @return 用户列表
 */
@GetMapping("/users/{userId}/{type}")
public Result<List<Author>> getFollowingList(@PathVariable("userId") Long userId,
                                             @PathVariable("type") String type) {
    List<Author> userList = null;
    try {
        if(type.equals("following")){
            userList = followService.getFollowingList(userId);
        } else if(type.equals("followers")){  // 注意这里是复数形式
            userList = followService.getFollowerList(userId);
        }

        return Result.success(userList);
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


/**
     * 搜索用户
     *
     * @param userId 当前用户ID
     * @param keyword 搜索关键词
     * @return 搜索结果列表
     */
    @GetMapping("/users/search")
    public Result<List<Author>> searchUsers(@CurrentUser Long userId,  @RequestParam("q") String keyword) {

        // 1. 参数校验
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        try {
            List<Author> searchResults = communityService.searchUsers(keyword, userId);
            return Result.success(searchResults);
        } catch (Exception e) {
            return Result.error(500, "搜索失败", e.getMessage());
        }
    }


}