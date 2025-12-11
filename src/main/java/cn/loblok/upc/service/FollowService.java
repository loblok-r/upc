package cn.loblok.upc.service;

import cn.loblok.upc.dto.Author;
import cn.loblok.upc.dto.FollowUserResponse;
import cn.loblok.upc.entity.Follow;
import cn.loblok.upc.entity.User;
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
    List<Author> getFollowingList(Long userId);

    /**
     * 获取推荐关注列表
     * @return 推荐关注列表
     */
    List<Author> getRecommendFollowList(Long userId);

}