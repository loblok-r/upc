package cn.loblok.upc.controller;


import cn.loblok.upc.constraints.CheckVerificationCodeGroup;
import cn.loblok.upc.constraints.SendVerificationCodeGroup;
import cn.loblok.upc.cto.VerificationCodeCTO;
import cn.loblok.upc.dto.*;
import cn.loblok.upc.enums.CommonStatusEnum;
import cn.loblok.upc.enums.VerificationCodeType;
import cn.loblok.upc.service.UserService;
import cn.loblok.upc.service.VerificationCodeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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

    @Autowired
    private VerificationCodeService verificationCodeService;




    
    /**
     * 用户注册接口
     * @param registerRequest 注册请求参数
     * @return 认证响应对象
     */
    @PostMapping("/register")
    public Result register( @Validated(CheckVerificationCodeGroup.class)
                                                 @RequestBody RegisterRequestDTO registerRequest) {

        log.info("用户注册: {}", registerRequest);

        if(!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())){
            return Result.error(CommonStatusEnum.PASSWORD_NOT_MATCH.getCode(),CommonStatusEnum.PASSWORD_NOT_MATCH.getMessage());
        }

        // 验证码校验
        if (!verificationCodeService.checkCode(registerRequest.getEmail(),
                registerRequest.getCode(),
                VerificationCodeType.register.getType())) {
            return Result.error(CommonStatusEnum.VERIFICATION_CODE_ERROR.getCode(),CommonStatusEnum.VERIFICATION_CODE_ERROR.getMessage());
        }

        try {

            return  userService.register(
                    registerRequest.getUsername(),
                    registerRequest.getPassword(),
                    registerRequest.getEmail()
            );

        } catch (Exception e) {
            return Result.error(CommonStatusEnum.CALL_USER_ADD_ERROR.getCode(),CommonStatusEnum.CALL_USER_ADD_ERROR.getMessage());

        }
    }
    
    /**
     * 用户登录接口
     * @param loginRequest 登录请求参数
     * @return 认证响应对象
     */
    @PostMapping("/login")
    public Result<AuthResponseDTO> login(@Validated(CheckVerificationCodeGroup.class)
                                             @RequestBody LoginRequestDTO loginRequest) {

            log.info("用户登录: {}", loginRequest);


        //验证码校验
        if (!verificationCodeService.checkCode(loginRequest.getEmail(),
                loginRequest.getCode(),
                VerificationCodeType.login.getType())) {
            return Result.error(CommonStatusEnum.VERIFICATION_CODE_ERROR.getCode(),CommonStatusEnum.VERIFICATION_CODE_ERROR.getMessage());
        }

        try {
            return userService.login(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            );
        } catch (Exception e) {
            throw new RuntimeException("登录失败: " + e.getMessage());
        }
    }

    /**
     * 忘记密码接口
     *
     * @param request 忘记密码请求参数
     * @return 重置结果
     */
    @PostMapping("/resetPassword")
    public Result resetPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        log.info("重置密码请求: email={}", request.getEmail());

        // 验证两次输入的密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            return Result.error("两次输入的密码不一致");
        }

        // 调用userService重置密码
        return userService.forgotPassword(request.getEmail(), request.getNewPassword(), request.getCode());
    }
}