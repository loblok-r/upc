package cn.loblok.upc.service;

import cn.loblok.upc.dto.AuthResponseDTO;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.entity.User;
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
     * @return 认证响应对象
     */
    Result register(String username, String password, String email);

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

    // 更新用户经验值
    void updateUserExp(Long userId, int newvalue);

    // 更新用户积分值
    void updateUserPoints(Long userId, int newvalue);

    // 更新用户积分值以及等级
    void updateUserExpAndLevel(Long userId, int i, String newLevel);
}