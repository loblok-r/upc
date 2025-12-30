package cn.loblok.upc.auth.dto;

import cn.loblok.upc.auth.constraints.CheckVerificationCodeGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 忘记密码请求参数
 */
@Data
public class ForgotPasswordRequestDTO {
    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不可以为空")
    @Pattern(regexp = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$", message = "请输入正确的邮箱")
    @Schema(description = "邮箱")
    private String email;

    /**
     * 新密码
     */
    @NotBlank(message = "密码不可以为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "密码格式错误")
    @Schema(description = "新密码")
    private String newPassword;

    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不可以为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "密码格式错误")
    @Schema(description = "确认密码")
    private String confirmNewPassword;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不可以为空", groups = {CheckVerificationCodeGroup.class})
    @Pattern(regexp = "^\\d{6}$", message = "请输入6位验证码", groups = {CheckVerificationCodeGroup.class})
    @Schema(description = "验证码")
    private String code;
}