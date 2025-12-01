package cn.loblok.upc.service;

import cn.loblok.upc.dto.AuthResponseDTO;
import cn.loblok.upc.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author loblok
 * @since 2025-11-30
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     * @param username 用户名
     * @param password 密码
     * @param tenantId 租户ID
     * @return 认证响应对象
     */
    AuthResponseDTO register(String username, String password, String tenantId);

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 认证响应对象
     */
    AuthResponseDTO login(String username, String password);


}