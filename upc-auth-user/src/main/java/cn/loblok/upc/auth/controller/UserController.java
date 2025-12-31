package cn.loblok.upc.auth.controller;


import cn.loblok.upc.auth.constraints.SendVerificationCodeGroup;
import cn.loblok.upc.auth.dto.*;
import cn.loblok.upc.common.annotation.CurrentUser;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.auth.constraints.CheckVerificationCodeGroup;
import cn.loblok.upc.common.enums.CommonStatusEnum;
import cn.loblok.upc.common.enums.VerificationCodeType;
import cn.loblok.upc.auth.service.UserService;
import cn.loblok.upc.auth.service.VerificationCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


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
@Tag(name = "用户接口", description = "用户接口")
public class UserController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private VerificationCodeService verificationCodeService;


/**
     * 获取用户信息接口
     * @param userId 用户ID
     * @return 用户信息对象
     */
    @GetMapping("/profile")
    @Operation(summary = "获取用户信息")
    public Result<UserProfileDTO> getProfile(@CurrentUser Long userId){
        return userService.getUserInfo(userId);
    }



    /**
     * 修改用户信息接口
     * @param userId 当前用户ID
     * @param updateProfileRequest 更新用户信息请求参数
     * @return 更新结果
     */
    @PostMapping("/updateProfile")
    @Operation(summary = "修改用户信息")
    public Result<String> updateProfile(@CurrentUser Long userId, 
                                        @Valid @RequestBody UpdateProfileRequestDTO updateProfileRequest) {
        log.info("修改用户信息: userId={}, request={}", userId, updateProfileRequest);
        
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        try {
            //todo 待完善
//            return userService.updateUserProfile(userId, updateProfileRequest);
            return null;
        } catch (Exception e) {
            return Result.error("修改用户信息失败: " + e.getMessage());
        }
    }


    /**
     * 用户注册接口
     * @param registerRequest 注册请求参数
     * @return 认证响应对象
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<RegisterResponse> register(@Validated(CheckVerificationCodeGroup.class)
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
     * 获取用户资源接口
     * @param userId 用户ID
     * @return 用户资源对象
     */
    @GetMapping("/resources")
    @Operation(summary = "获取用户资源")
    public Result<UserResourcesDTO> getResources(@CurrentUser Long userId) {
        return userService.getResources(userId);
    }


    /**
     * 用户登录接口
     * @param loginRequest 登录请求参数
     * @return 认证响应对象
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<AuthResponseDTO> login(@Validated(CheckVerificationCodeGroup.class)
                                             @RequestBody LoginRequestDTO loginRequest) {

            log.info("用户登录: {}", loginRequest);


        //验证码校验
        if (!verificationCodeService.checkCode(loginRequest.getEmail(),
                loginRequest.getCode(),
                VerificationCodeType.login.getType())) {
            return Result.error(CommonStatusEnum.VERIFICATION_CODE_ERROR.getCode(),CommonStatusEnum.VERIFICATION_CODE_ERROR.getMessage());
        }
        log.info("登录验证码校验通过..");

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
    @Operation(summary = "忘记密码")
    public Result resetPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        log.info("重置密码请求: email={}", request.getEmail());

        // 验证两次输入的密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            return Result.error("两次输入的密码不一致");
        }

        // 调用userService重置密码
        return userService.forgotPassword(request.getEmail(), request.getNewPassword(), request.getCode());
    }

    /**
     * 用户注销接口
     * @param userId 当前用户ID
     * @return 注销结果
     */
    @PostMapping("/logout")
    @Operation(summary = "用户注销")
    public Result<String> logout(@CurrentUser Long userId) {
        log.info("用户注销: userId={}", userId);
        
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        try {
            //todo 添加用户注销逻辑
//            userService.logout(userId);
            return Result.success("注销成功");
        } catch (Exception e) {
            return Result.error("注销失败: " + e.getMessage());
        }
    }



    /**
     * 获取验证码
     * @param verificationCodeCTO 验证码请求参数
     * @return 验证码响应对象
     */
    @PostMapping("/sendCode")
    @Operation(summary = "获取验证码")
    public Result sendCode(@Validated(SendVerificationCodeGroup.class)
                           @RequestBody VerificationCodeCTO verificationCodeCTO) {
        log.info("用户请求发送验证码: {}", verificationCodeCTO);

        // 验证邮箱不为空
        if (!StringUtils.hasText(verificationCodeCTO.getEmail())) {
            return Result.error(400, "邮箱地址不能为空");
        }

        return verificationCodeService.generationCode(verificationCodeCTO.getEmail(), verificationCodeCTO.getType());
    }



    /**
     * 头像上传接口
     * @param userId 当前用户ID
     * @param avatarFile 头像文件
     * @return 上传结果
     */
    @PostMapping("/uploadAvatar")
    @Operation(summary = "上传头像")
    public Result<String> uploadAvatar(@CurrentUser Long userId, 
                                       @RequestParam("avatar") MultipartFile avatarFile) {
        log.info("上传头像: userId={}, fileName={}", userId, avatarFile.getOriginalFilename());
        
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        if (avatarFile == null || avatarFile.isEmpty()) {
            return Result.error("请选择要上传的头像文件");
        }
        
        try {
            // 检查文件类型
            String contentType = avatarFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("请选择图片文件");
            }
            
            // 检查文件大小，限制为5MB
            long maxSize = 5 * 1024 * 1024;
            if (avatarFile.getSize() > maxSize) {
                return Result.error("头像文件大小不能超过5MB");
            }

            //todo 添加用户上传头像逻辑
//            return userService.uploadAvatar(userId, avatarFile);
            return null;
        } catch (Exception e) {
            log.error("上传头像失败", e);
            return Result.error("上传头像失败: " + e.getMessage());
        }
    }
}