package cn.loblok.upc.user;

import cn.loblok.upc.common.dto.AuthResponseDTO;
import cn.loblok.upc.common.dto.LoginRequestDTO;
import cn.loblok.upc.common.dto.RegisterRequestDTO;
import cn.loblok.upc.common.dto.Result;
import cn.loblok.upc.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *   用户接口
 * </p>
 *
 * @author loblok
 * @since 2025-11-30
 */
@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户注册接口
     * @param registerRequest 注册请求参数
     * @return 认证响应对象
     */
    @PostMapping("/register")
    public Result<AuthResponseDTO> register(@RequestBody RegisterRequestDTO registerRequest) {
        try {
            AuthResponseDTO register = userService.register(
                    registerRequest.getUsername(),
                    registerRequest.getPassword(),
                    registerRequest.getTenantId()
            );
            return Result.success(register);
        } catch (Exception e) {
            throw new RuntimeException("注册失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户登录接口
     * @param loginRequest 登录请求参数
     * @return 认证响应对象
     */
    @PostMapping("/login")
    public Result<AuthResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            AuthResponseDTO login = userService.login(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );
            return Result.success(login);
        } catch (Exception e) {
            throw new RuntimeException("登录失败: " + e.getMessage());
        }
    }
}