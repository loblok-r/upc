package cn.loblok.upc.community.service;

import cn.loblok.upc.api.user.dto.UserPublicInfoDTO;
import cn.loblok.upc.community.dto.FollowUserResponse;
import cn.loblok.upc.community.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户关注关系表 服务类
 * </p>
 *
 * @author loblok
 * @since 2025-12-11
 */
public interface FollowService extends IService<Follow> {

    /**
     * 关注或取消关注用户
     * @param followerId 关注者ID（当前用户）
     * @param followeeId 被关注者ID（目标用户）
     * @return FollowUserResponse对象，包含操作结果和被关注者的粉丝数
     */
    FollowUserResponse followOrUnfollow(Long followerId, Long followeeId);
    
    /**
     * 获取用户关注的用户列表
     * @param userId 用户ID
     * @return 关注的用户列表
     */
    List<UserPublicInfoDTO> getFollowingList(Long userId);

    /**
     * 获取用户关注的用户ID列表
     * @param
     * @return 关注的用户ID列表
     */

    List<Long> findFollowedIds(Long currentUserId);

    /**
     * 根据用户ID列表获取已关注的用户ID列表
     * @param currentUserId 当前用户ID
     * @param userIds 用户ID列表
     * @return 已关注的用户ID列表
     */
    List<Long> findFollowedIds(Long currentUserId, List<Long> userIds);



    /**
     * 获取用户粉丝列表
     * @param userId 用户ID
     * @return 粉丝列表
     */
    List<UserPublicInfoDTO> getFollowerList(Long userId);


    /**
     * 获取用户粉丝Id列表
     * @param userId 用户ID
     * @return 关注的用户ID列表
     */
    List<Long> findFlowerIds(Long userId) ;



    /**
     * 获取推荐关注列表
     * @return 推荐关注列表
     */
    List<UserPublicInfoDTO> getRecommendFollowList(Long userId);


    /**
     * 判断用户是否已关注某个用户
     * @param userId 用户ID
     * @param targetId 目标用户ID
     * @return 是否已关注
     */
    boolean isFollowed(Long userId, Long targetId);



}