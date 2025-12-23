package cn.loblok.upc.community.service;

import cn.loblok.upc.api.user.dto.UserPublicInfoDTO;
import cn.loblok.upc.community.dto.CommunityUserVO;
import cn.loblok.upc.community.dto.CreatorLeaderboardResponse;
import cn.loblok.upc.community.dto.PostResponse;
import cn.loblok.upc.common.base.Result;

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
    CommunityUserVO getUserProfile(Long targetUserId, Long currentUserId);

    
    /**
     * 搜索用户
     * @param keyword 搜索关键词
     * @param currentUserId 当前用户ID
     * @return 搜索结果列表
     */
    List<CommunityUserVO> searchUsers(String keyword,int page,int pageSize, Long currentUserId);
}