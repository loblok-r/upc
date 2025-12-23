package cn.loblok.upc.auth.service;

import cn.loblok.upc.api.user.dto.UserPublicInfoDTO;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.auth.entity.User;
import cn.loblok.upc.auth.dto.RegisterResponse;
import cn.loblok.upc.auth.dto.UserProfileDTO;
import cn.loblok.upc.auth.dto.UserResourcesDTO;
import cn.loblok.upc.common.enums.AppMode;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author loblok
 * @since 2025-11-30
 */
public interface UserService extends IService<User> {

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    User getById(Long userId);

    /**
     * 用户注册
     *
     * @param username 用户名
     * @param password 密码
     * @return 认证响应对象
     */
    Result<RegisterResponse> register(String username, String password, String email);

    /**
     * 用户登录
     *
     * @param email 用户名
     * @param password 密码
     * @return 认证响应对象
     */
    Result login(String email, String password);

    /**
     * 忘记密码
     *
     * @param email 邮箱
     * @param newPassword 新密码
     * @param code 验证码
     * @return 操作结果
     */
    Result forgotPassword(String email, String newPassword, String code);

    /**
     * 更新用户经验值
     *
     * @param userId 用户ID
     * @param newvalue 新经验值
     */
    void updateUserExp(Long userId, int newvalue);

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    Result<UserPublicInfoDTO> getUserPublicInfo(Long userId);



    Result<UserProfileDTO> getUserInfo(Long userId);



    /**
     * 更新用户经验值和等级
     *
     * @param userId 用户ID
     * @param i 新经验值
     * @param newLevel 新等级
     */
    void updateUserExpAndLevel(Long userId, int i, String newLevel);

    /**
     * 消耗用户算力值
     *
     * @param userId 用户ID
     * @param amount 新算力值
     */
    Result<Void> consumeComputerPower(Long userId, Integer amount);
    /**
     * 添加用户算力值
     *
     * @param userId 用户ID
     * @param amount 新算力值
     */
    Result<Boolean> addComputingPower(Long userId, Integer amount);

    /**
     * 添加用户会员天数
     *
     * @param userId 用户ID
     * @param days 新会员天数
     */
    Result<Void> extendVipDays(Long userId, Integer days);

    /**
     * 判断用户是否为会员
     *
     * @param userId 用户ID
     * @return 是否为会员
     */
    Boolean isMember(Long userId);

    /**
     * 获取用户资源
     *
     * @param userId 用户ID
     * @return 用户资源
     */
    Result<UserResourcesDTO> getResources(Long userId);


    /**
     * 更新用户作品数
     *
     * @param userId 用户ID
     * @param delta 增加数量
     */
    void updateUserWorkCounts(Long userId, Integer delta);

    /**
     * 获取推荐用户
     *
     * @param limit 限制数量
     * @param excludeIds 排除用户ID
     * @return 推荐用户
     */
    Result<List<UserPublicInfoDTO>> getRecommendedUsers(Integer limit, List<Long> excludeIds);

    /**
     * 更新用户点赞数
     *
     * @param userId 用户ID
     * @param delta 增加数量
     */
    void updateLikeCounts(Long userId, Integer delta);

    /**
     * 更新用户关注数
     *
     * @param userId 用户ID
     * @return 关注数
     */
    void updateFollowingsCounts(Long userId, Integer delta);

    /**
     * 更新用户粉丝数
     *
     * @param userId 用户ID
     * @return 粉丝数
     */
    void updateFollowersCounts(Long userId, Integer delta);

    Result<Void> checkAiAccess(Long userId, AppMode mode,int amount);


    Result<Integer> consumeLotteryTicket(Long userId);
}