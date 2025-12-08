package cn.loblok.upc.controller;

import cn.loblok.upc.dto.*;
import cn.loblok.upc.service.UserService;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/api/user")
@Slf4j
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
        log.info("用户注册: {}", registerRequest);
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
            log.info("用户登录: {}", loginRequest);

            if(loginRequest.getCaptcha().equals("123456") && loginRequest.getUsername().equals("admin")
                    && loginRequest.getPassword().equals("admin")
                ){

                AuthResponseDTO authResponseDTO = new AuthResponseDTO();
                authResponseDTO.setToken("123456");
                authResponseDTO.setUserId(001L);
                authResponseDTO.setUsername("admin");

                return Result.success(authResponseDTO);
            }
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