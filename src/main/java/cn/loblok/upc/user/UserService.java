package cn.loblok.upc.user;

import cn.loblok.upc.common.dto.AuthResponseDTO;
import com.baomidou.mybatisplus.extension.service.IService;

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
     * @param tenantId 租户ID
     * @return 认证响应对象
     */
    AuthResponseDTO register(String username, String password, String tenantId);

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 认证响应对象
     */
    AuthResponseDTO login(String username, String password);


    // 更新用户经验值
    void updateUserExp(Long userId, int newvalue);

    // 更新用户积分值
    void updateUserPoints(Long userId, int newvalue);

    // 更新用户积分值以及等级
    void updateUserExpAndLevel(Long userId, int i, String newLevel);
}