package cn.loblok.upc.service;

import cn.loblok.upc.dto.Author;
import cn.loblok.upc.dto.CreatorLeaderboardResponse;
import cn.loblok.upc.dto.PostResponse;
import cn.loblok.upc.dto.Result;

import java.util.List;

public interface CommunityService {

    /**
     * 获取创作者排行榜
     * @return 创作者排行榜
     */
    List<CreatorLeaderboardResponse> getCreatorLeaderboard(Long userId);

    /**
     * 获取新创作者排行榜
     * @return 新创作者排行榜
     */
    List<CreatorLeaderboardResponse> getNewCreatorLeaderboard(Long userId);


    /**
     * 点赞或取消点赞评论
     * @param userId 当前用户ID
     * @param commentId 评论ID
     * @return 操作结果
     */

    String likeOrUnlikeComment(Long userId, Long commentId);

    /**
     * 获取用户个人资料
     * @param targetUserId 目标用户ID
     * @param currentUserId 当前用户ID
     * @return 用户个人资料
     */
    Author getUserProfile(Long targetUserId, Long currentUserId);

    /**
     * 获取用户作品列表
     * @param targetUserId 目标用户ID
     * @param currentUserId 当前用户ID
     * @return 用户作品列表
     */
    Result<List<PostResponse>> getUserWorks(Long targetUserId, Long currentUserId);
    
    /**
     * 搜索用户
     * @param keyword 搜索关键词
     * @param currentUserId 当前用户ID
     * @return 搜索结果列表
     */
    List<Author> searchUsers(String keyword, Long currentUserId);
}